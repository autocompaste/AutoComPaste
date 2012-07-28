using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using ACPAddIn.Object_Class;
using System.ComponentModel;
using System.Globalization;
using System.Windows.Forms;
using System.Drawing;


/// <summary> This class is use to handle extending and reducing of sentences in ACP.</summary>
/// <remarks>
/// Author: Teo Kee Cheng
/// </remarks>
namespace ACPAddIn
{
    class ExtensionMode
    {
        private bool isExtensionFail = false;

        // constant variables for extend suggestion
        public const int EXTENDPARAGRAPH = 1;
        public const int REMOVEPARAGRAPH = 2;
        public const int EXTENDSENTENCE = 3;
        public const int REMOVESENTENCE = 4;
        public const int EXTENDWORD = 5;
        public const int REMOVEWORD = 6;

        // This variable keeps track of the extra spaces added to each sentences
        public const String extraSpace = " ";
        //private static const int extraSpaceCount = extraSpace.Length;
        public const char wordListSplitChar = ' ';

        // This variable keeps track of whether it is in extension mode
        private bool extensionMode = false;
        private Word.Range extensionRange = null;
        private List<Suggestion> extensions = null;
        private List<int> paragraphPos = null; // keep track of extensionPos with additional paragraph space
        private int paragraphSpace = 2; // to store each additional paragraph space cost range value of 1
        private int extensionType = -1;
        private int extensionPos = -1;
        private int extensionWordPos = -1;
        private List<string> currentExtensionPosWordList = null;
        private bool isCompletingLastSentenceOfPara = false;
        public int isRetrieving = 1;

        private Client logic;
        public ExtensionMode(Client logic)
        {
            this.logic = logic;
        }

        public Word.Range getExtensionRange()
        {
            return extensionRange;
        }
        public void setExtensionRange(Word.Range range)
        {
            this.extensionRange = range;
        }

        public bool isExtensionMode()
        {
            return extensionMode;
        }

        public int getExtensionPos()
        {
            return extensionPos;
        }

        public void extendSuggestion(int code)
        {
            switch (code)
            {
                case EXTENDPARAGRAPH:
                    extendParagraph();
                    break;
                case REMOVEPARAGRAPH:
                    removeParagraph();
                    break;
                case EXTENDSENTENCE:
                    extendSentence();
                    break;
                case REMOVESENTENCE:
                    removeSentence();
                    break;
                case EXTENDWORD:
                    extendWord();
                    break;
                case REMOVEWORD:
                    removeWord();
                    break;
            }
        }

        private void extendParagraph()
        {
            extensionType = Suggestion.PARAGRAPH; 
            isCompletingLastSentenceOfPara = false; // reset to false everytime when extending a paragraph

            // if it is extending words and extensionWordPos is not at the last word of the sentence
            if (extensionWordPos != -1 && extensionWordPos != currentExtensionPosWordList.Count-1)
            {
                displayExtension(null, false); // finish extending the current sentence
            }

            retrieveExtendSuggestion();
        }

        private void removeParagraph()
        {
            bool isTwoLatestSentenceDifferentPara = false; // keeps track if the last two sentences are from different paragraph

            Word.Range range = Globals.ThisAddIn.Application.ActiveDocument.Range(extensionRange.End, extensionRange.End);

            if (extensionWordPos != -1) // if is extending some words
            {
                // compare last two sentences
                Sentence currentSentence = (Sentence)extensions[extensionPos];
                Sentence previousSentence = (Sentence)extensions[extensionPos-1];
                if (currentSentence.paragraphID != previousSentence.paragraphID)
                    isTwoLatestSentenceDifferentPara = true;

                // remove extended words from current sentence
                for (int i = extensionWordPos; i >= 0; i--)
                {
                    String word = currentExtensionPosWordList[i];
                    shiftRange(range, new StringInfo(word).LengthInTextElements);
                }

                // reset variables used for extending word
                currentExtensionPosWordList = null;
                extensionWordPos = -1;

                extensionPos--; // move focus to previous sentence
            }

            if (!isTwoLatestSentenceDifferentPara)
            {
                Sentence lastExtendSentence = (Sentence)extensions[extensionPos];
                int lastExtendSentenceParaID = lastExtendSentence.paragraphID;
                int previousSentenceParaID = -1;

                // putting sentence one by one into the range for removal
                do
                {
                    shiftRange(range, new StringInfo(((Sentence)extensions[extensionPos]).content).LengthInTextElements);
                    extensionPos--;

                    checkAndRemoveParagraphSpace(range);

                    // retrieve paragraph id of the sentence before
                    if (extensionPos >= 0)
                    {
                        lastExtendSentence = (Sentence)extensions[extensionPos];
                        previousSentenceParaID = lastExtendSentence.paragraphID;
                    }
                    else
                        break;

                } while (lastExtendSentenceParaID == previousSentenceParaID);
            }

            removeRangeTextAndRepositionCursor(range);
        }

        private void extendSentence()
        {
            extensionType = Suggestion.SENTENCE;

            // if current extensionPos is at last item of extensions, not in extend word mode, or is extending 
            // last word of the sentence, then go fetch data
            if (extensionPos == (extensions.Count - 1) && 
                extensionWordPos == -1 && 
                (currentExtensionPosWordList == null || extensionWordPos == currentExtensionPosWordList.Count - 1)) 
            {
                retrieveExtendSuggestion();
            }
            else // else retrieve from extension list the next extension
            {
                // if not extending word
                if (extensionWordPos == -1)
                {
                    List<Suggestion> nextExtension = new List<Suggestion>();
                    nextExtension.Add(extensions[extensionPos + 1]);
                    displayExtension(nextExtension, false);
                }
                else // if is extending word
                {
                    // extend current incomplete sentence
                    displayExtension(null, false);
                }
            }
        }

        private void removeSentence()
        {
            Word.Range range = Globals.ThisAddIn.Application.ActiveDocument.Range(extensionRange.End, extensionRange.End);

            // if not extending word
            if (extensionWordPos == -1)
            {
                shiftRange(range, new StringInfo(((Sentence)extensions[extensionPos]).content).LengthInTextElements);
            }
            else // if is extending word
            {
                // remove extended words from current sentence
                for(int i = extensionWordPos; i >= 0; i--)
                {
                    String word = currentExtensionPosWordList[i];
                    shiftRange(range, new StringInfo(word).LengthInTextElements);
                }

                // reset extend word 
                currentExtensionPosWordList = null;
                extensionWordPos = -1;
            }

            extensionPos--;

            checkAndRemoveParagraphSpace(range);

            removeRangeTextAndRepositionCursor(range);
        }

        private void extendWord()
        {
            extensionType = Suggestion.WORD;

            // if current extensionPos is at last item of extension
            if (extensionPos == (extensions.Count - 1)) 
            {
                // To retrieve new extension if current word list is null or not null but extensionWordPos is the last index of the current word list
                if (currentExtensionPosWordList == null ||
                    (currentExtensionPosWordList != null &&
                     extensionWordPos == currentExtensionPosWordList.Count - 1))
                {
                    retrieveExtendSuggestion();
                }
                else // display next word since extensionWordPos is not at the last index of the currentExtensionPosWordList
                {
                    displayExtension(null, false);
                }
            }
            else // else retrieve from extension list the next word extension
            {
                displayExtension(null, false);
            }
        }

        private void removeWord()
        {
            Word.Range range = Globals.ThisAddIn.Application.ActiveDocument.Range(extensionRange.End, extensionRange.End);

            // if not extending word
            if (extensionWordPos == -1)
            {
                Sentence currentSentence = (Sentence)extensions[extensionPos];
                currentExtensionPosWordList = currentSentence.content.Split(wordListSplitChar).ToList();
                extensionWordPos = currentExtensionPosWordList.Count - 1;

                shiftRange(range, new StringInfo(currentExtensionPosWordList[extensionWordPos]).LengthInTextElements);
        
                extensionWordPos--;

                if (extensionWordPos == -1)
                {
                    extensionPos--;
                    checkAndRemoveParagraphSpace(range);
                }
            }
            else if (extensionWordPos == 0) // if at position where only extension of one word
            {
                shiftRange(range, new StringInfo(currentExtensionPosWordList[extensionWordPos]).LengthInTextElements);
                currentExtensionPosWordList = null;
                extensionWordPos = -1;
                extensionPos--;

                checkAndRemoveParagraphSpace(range);
            }
            else // if extended more than one word 
            {
                shiftRange(range, new StringInfo(currentExtensionPosWordList[extensionWordPos]).LengthInTextElements);
                extensionWordPos--;
            }

            removeRangeTextAndRepositionCursor(range);
        }

        private void checkAndRemoveParagraphSpace(Word.Range range)
        {
            if (paragraphPos.Count > 0)
            {
                if (extensionPos == paragraphPos[paragraphPos.Count - 1])
                {
                    range.MoveStart(Word.WdUnits.wdCharacter, -paragraphSpace);
                    range.MoveEnd(Word.WdUnits.wdCharacter, 1);

                    extensionRange.MoveEnd(Word.WdUnits.wdCharacter, -paragraphSpace);

                    paragraphPos.RemoveAt(paragraphPos.Count - 1);
                }
            }
        }

        private void shiftRange(Word.Range range, int length)
        {
            range.MoveStart(Word.WdUnits.wdCharacter, -(length + extraSpace.Length));
            extensionRange.MoveEnd(Word.WdUnits.wdCharacter, -(length + extraSpace.Length));
        }

        public void removeRangeTextAndRepositionCursor(Word.Range range)
        {
            range.Delete();

            // Reposition the cursor to the end of the sentence
            int position = extensionRange.End;
            Globals.ThisAddIn.Application.Selection.SetRange(position, position);
            highlight(extensionRange);
            scrollToRange(extensionRange);

            isRetrieving = 1;

            if (extensionPos == -1)
                resetExtensionMode();
        }

        private void retrieveExtendSuggestion()
        {
            Suggestion lastExtension = null;
            List<Suggestion> nextExtensions = null;

            try
            {
                switch (extensionType)
                {
                    case Suggestion.PARAGRAPH:
                        lastExtension = extensions[extensionPos];
                        nextExtensions = logic.requestExtendSuggestion(lastExtension.id, Suggestion.PARAGRAPH);
                        
                        // if is completing the last sentence of paragraph
                        if (isCompletingLastSentenceOfPara)
                        {
                            if (nextExtensions.Count > 0)
                            {
                                Sentence lastSentence = (Sentence)lastExtension;
                                Sentence firstSentenceOfNextPara = (Sentence)nextExtensions[0];

                                if (lastSentence.paragraphID != firstSentenceOfNextPara.paragraphID)
                                {
                                    return;
                                }
                            }

                            isCompletingLastSentenceOfPara = false;
                        }

                        break;
                    case Suggestion.SENTENCE:
                        // Send network request to Java ACP to request for suggestions
                        lastExtension = extensions[extensionPos];
                        nextExtensions = logic.requestExtendSuggestion(lastExtension.id, Suggestion.SENTENCE);
                        break;
                    case Suggestion.WORD:
                        lastExtension = extensions[extensionPos];
                        nextExtensions = logic.requestExtendSuggestion(lastExtension.id, Suggestion.SENTENCE);
                        break;
                    default:
                        Console.WriteLine("retrieveExtendSuggestion: Suggestion type undefined");
                        break;
                }

                if (lastExtension != null && nextExtensions != null)
                {
                    // display suggestions
                    displayExtension(nextExtensions, true);
                }
            }
            catch (System.Net.Sockets.SocketException e)
            {
                // Inform UI Thread to display error message
                if (!isExtensionFail)
                {
                    isExtensionFail = true;
                    Globals.ThisAddIn.Dispatcher.Invoke(new displayErrorMessageDelegate(displayErrorMessage), new Object[] { "Unable to communicate to the ACP backend processes", "ACP is not running on the background." });
                }
            }
        }


        private void displayExtension(List<Suggestion> nextExtensions, bool addIntoExtensions)
        {
            // if it is a extension of word NOT from the last extensionPos and last extensionWordPos
            if (nextExtensions == null)
            {
                displayWordExtensions(); // complete word extension to complete sentence or just extend one word
            }
            else if (nextExtensions.Count == 0) // if there are no subsequent suggestion
            {
                Globals.ThisAddIn.Dispatcher.Invoke(new displayMessageDelegate(displayMessage), new Object[] { "No subsequent suggestion available" });
            }
            else
            {
                displayParaSentenceExtensions(nextExtensions, addIntoExtensions);
            }

            isRetrieving = 1;
        }

        // To allow background worker thread to display the message on the UI thread
        private delegate void displayMessageDelegate(String message);
        private void displayMessage(String message)
        {
            Globals.ThisAddIn.getNotificationForm().setMessage(message);
            Globals.ThisAddIn.getNotificationForm().updateLocation(Globals.ThisAddIn.getApplicationLocation(), Globals.ThisAddIn.getApplicationSize());
            Globals.ThisAddIn.getNotificationForm().showWithTimer(3);
        }

        // To allow background worker threads to display error message
        private delegate void displayErrorMessageDelegate(string msg, string title);
        private void displayErrorMessage(string msg, string title)
        {
            MessageBox.Show(msg, title);
        }

        private void displayParaSentenceExtensions(List<Suggestion> nextExtensions, bool addIntoExtensions)
        {
            Word.Range range = Globals.ThisAddIn.Application.ActiveDocument.Range(extensionRange.End, extensionRange.End);
            Sentence lastExtendedSentence = (Sentence)extensions[extensionPos];

            range.ParagraphFormat.SpaceAfter = 0;

            switch (extensionType)
            {
                case Suggestion.PARAGRAPH:
                    if (nextExtensions.Count > 0)
                    {
                        if (((Sentence)nextExtensions[0]).paragraphID == lastExtendedSentence.paragraphID)
                        {
                            bool canStartExtending = false; // for usage to extend paragraph that is similar with the last sentence

                            foreach (Suggestion e in nextExtensions)
                            {
                                Sentence s = (Sentence)e;

                                if (!canStartExtending)
                                {
                                    if (String.Compare(s.content, lastExtendedSentence.content, false) == 0)
                                        canStartExtending = true;
                                }
                                else // if(canStartExtending)
                                {
                                    range.Text += s.content + extraSpace;
                                    extensionPos++;

                                    // if extensionPos hit more than or equal to the count of extension list, add the sentence
                                    if (extensions.Count <= extensionPos)
                                        extensions.Add(s);
                                }

                            }
                        }
                        else // if (((Sentence)nextExtensions[0]).paragraphID != lastExtendedSentence.paragraphID) 
                        {
                            range.InsertParagraphAfter();
                            range.InsertParagraphAfter();
                            paragraphPos.Add(extensionPos); // store extensionPos that have additional 1 paragraph space

                            foreach (Suggestion e in nextExtensions)
                            {
                                Sentence s = (Sentence)e;

                                range.Text += s.content + extraSpace;
                                extensionPos++;

                                // if extensionPos hit more than or equal to the count of extension list, add the sentence
                                if (extensions.Count <= extensionPos)
                                    extensions.Add(e);
                            }
                        }
                    }
                    break;
                case Suggestion.SENTENCE:
                    foreach (Suggestion e in nextExtensions) // Will only have one suggestion
                    {
                        range.Text = ((Sentence)e).content + extraSpace;
                        extensionPos++;

                        if (addIntoExtensions)
                            extensions.Add(e);
                    }
                    break;
                case Suggestion.WORD:
                    foreach (Suggestion e in nextExtensions) // Will only have one suggestion
                    {
                        extensionWordPos = 0;

                        currentExtensionPosWordList = ((Sentence)e).content.Split(wordListSplitChar).ToList();
                        range.Text = currentExtensionPosWordList[extensionWordPos] + extraSpace;
                        extensionPos++;

                        if (addIntoExtensions)
                            extensions.Add(e);
                    }
                    break;
                default:
                    Console.WriteLine("displayExtension: Suggestion type undefined");
                    break;

            }

            if (range.Text != null)
            {
                extensionRange.MoveEnd(Word.WdUnits.wdCharacter, new StringInfo(range.Text).LengthInTextElements);
            }

            // Reposition the cursor to the end of the sentence that is just pasted
            int position = extensionRange.End;
            Globals.ThisAddIn.Application.Selection.SetRange(position, position);
            highlight(extensionRange);

            scrollToRange(extensionRange);
        }

        private void displayWordExtensions()
        {
            Word.Range range = Globals.ThisAddIn.Application.ActiveDocument.Range(extensionRange.End, extensionRange.End);

            switch (extensionType)
            {
                case Suggestion.PARAGRAPH: // Paragraph and sentence will do almost similar codes
                    for (int i = extensionWordPos + 1; i < currentExtensionPosWordList.Count; i++)
                    {
                        String word = currentExtensionPosWordList[i];
                        range.Text += word + extraSpace;
                    }
                    currentExtensionPosWordList = null;
                    extensionWordPos = -1;
                    isCompletingLastSentenceOfPara = true;
                    break;
                case Suggestion.SENTENCE:
                    for (int i = extensionWordPos + 1; i < currentExtensionPosWordList.Count; i++)
                    {
                        String word = currentExtensionPosWordList[i];
                        range.Text += word + extraSpace;
                    }
                    currentExtensionPosWordList = null;
                    extensionWordPos = -1;
                    break;
                case Suggestion.WORD:
                    if (extensionWordPos == -1 || extensionWordPos == currentExtensionPosWordList.Count - 1)
                    {
                        extensionWordPos = 0;
                        extensionPos++;

                        Sentence nextSentence = (Sentence)extensions[extensionPos];
                        currentExtensionPosWordList = nextSentence.content.Split(wordListSplitChar).ToList();
                        range.Text = currentExtensionPosWordList[extensionWordPos] + extraSpace;

                    }
                    else // if (extensionWordPos != lastWordListIndex && extensionWordPos != -1)
                    {
                        extensionWordPos++;
                        range.Text = currentExtensionPosWordList[extensionWordPos] + extraSpace;
                    }
                    break;
            }

            if (range.Text != null)
                extensionRange.MoveEnd(Word.WdUnits.wdCharacter, new StringInfo(range.Text).LengthInTextElements);


            // Reposition the cursor to the end of the sentence that is just pasted
            int position = extensionRange.End;
            Globals.ThisAddIn.Application.Selection.SetRange(position, position);
            highlight(extensionRange);

            scrollToRange(extensionRange);
        }

        public static void highlight(Word.Range range)
        {
            range.Select();
            //range.HighlightColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdGray25;
        }

        public static void unhighlight(Word.Range range)
        {
            //range.HighlightColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdNoHighlight;
            Globals.ThisAddIn.Application.Selection.SetRange(range.End, range.End);
        }

        private void scrollToRange(Word.Range range)
        {
            Word.Range endRange = Globals.ThisAddIn.Application.ActiveDocument.Range(range.End, range.End);

            Word.Window win = Globals.ThisAddIn.Application.ActiveWindow;
            win.ScrollIntoView(endRange);
        }

        public void setupExtensionMode(Suggestion suggestion)
        {
            // Turn on extension mode
            extensionMode = true;
            extensions = new List<Suggestion>();
            paragraphPos = new List<int>();

            // Add suggestion to extensions
            extensions.Add(suggestion);
            extensionPos = 0;
        }

        public void resetExtensionMode()
        {
            unhighlight(extensionRange);
            extensionRange = null;
            extensionMode = false;
            extensions = null;
            paragraphPos = null;
            currentExtensionPosWordList = null;
            isCompletingLastSentenceOfPara = false;
            extensionType = -1;
            extensionPos = -1;
            extensionWordPos = -1;
        }
    }
}
