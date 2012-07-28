using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using ACPAddIn.Object_Class;
using System.Collections;
using System.Drawing;

namespace ACPAddIn
{
    /// <summary>
    /// This class handle the presentation layer for presenting the Suggestion list in a cusotmized window form to user.
    /// It provides methods such as to navigate the page, hide the suggestion box and text wrapping for each entry.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    public partial class AutoCompleteForm : Form
    {
        private List<Suggestion> suggestions;
        private List<Suggestion> displaySuggestion;
        private int pageIndex;
        private int numOfPage;
        private int padding = 3;

        private bool isHoverNext = false;
        private bool isHoverPrevious = false;

        private bool isSubStringMode = true;

        private PreviewPanel previewPanel;

        public AutoCompleteForm()
        {
            InitializeComponent();

            listBox1.DrawMode = DrawMode.OwnerDrawFixed;
            listBox1.DrawItem += new DrawItemEventHandler(ListBox1_DrawItem);
            listBox1.ItemHeight = 35;

            listBox1.SelectedIndexChanged += new System.EventHandler(ListBox1_SelectedIndexChanged);

            previewPanel = new PreviewPanel();
            previewPanel.Location = new Point(-1000, -1000);
            previewPanel.Show(this.Owner);

            BackColor = Color.Lime;
            panel1.BackColor = Color.Lime;
            TransparencyKey = Color.Lime;

            this.displaySuggestion = new List<Suggestion>();
        }

        #region Form Functions
        public void showForm(IWin32Window owner)
        {
            this.Show(owner);

            // Assign the owner of preview panel
            if (previewPanel != null && !previewPanel.Visible)
            {
                previewPanel.Show(owner);
                previewPanel.Hide();
            }
        }

        // This width include the preview panel width
        public int getActualWidth()
        {
            return this.Width - 4 + previewPanel.Width;
        }

        public void updateConfiguration()
        {
            // Without substring mode, the horizontal bar is show.
            if (!isSubStringMode)
            {
                listBox1.HorizontalScrollbar = true;
                this.Size = new Size(this.Width, 16 + Globals.ThisAddIn.EntriesInView * 42);
            }
            else
            {
                listBox1.HorizontalScrollbar = false;
                this.Size = new Size(this.Width, 16 + Globals.ThisAddIn.EntriesInView * (listBox1.ItemHeight+1) + 18);
                listBox1.IntegralHeight = false;
                listBox1.Size = new Size(330, Globals.ThisAddIn.EntriesInView * (listBox1.ItemHeight+1));
                previewPanel.Size = new Size(previewPanel.Width, listBox1.Height);
                statusLabel.Location = new Point(0, 16 + Globals.ThisAddIn.EntriesInView * (listBox1.ItemHeight+1) - 1);
                this.Size = new Size(330, statusLabel.Location.Y + statusLabel.Height);
            }
        }
        #endregion

        #region Override Forms Property
        protected override bool ShowWithoutActivation
        {
            get { return true; }
        }

        private const int CS_DROPSHADOW = 0x00020000;
        protected override CreateParams CreateParams
        {
            get
            {
                CreateParams baseParams = base.CreateParams;
                baseParams.ClassStyle |= CS_DROPSHADOW;

                return baseParams;
            }
        }
        #endregion

        #region Update Form with Suggestions
        public void populateForm(List<Suggestion> suggestions)
        {
            // To update the display settings based on the current configuration
            updateConfiguration();
            this.suggestions = suggestions;
            listBox1.SelectedIndex = -1;

            statusLabel.Text = "Press CTRL+[num] to select entry. Press Escape to hide.";

            int entriesInView = Globals.ThisAddIn.EntriesInView;

            numOfPage = Convert.ToInt32(Math.Ceiling(((Double)suggestions.Count / entriesInView)));
            pageIndex = 1;

            updateSuggestionPageDisplay();
            updateNextPreviousButton();

            previewPanel.Location = new Point(this.Location.X + this.Width - 4, this.Location.Y + 16);
            previewPanel.Hide();
        }
        #endregion

        #region Suggestion Paging
        public void nextSuggestionPage()
        {
            if (pageIndex < numOfPage)
            {
                pageIndex++;
                updateSuggestionPageDisplay();
                updateNextPreviousButton();
            }
        }

        public void previousSuggestionPage()
        {
            if (pageIndex > 1)
            {
                pageIndex--;
                updateSuggestionPageDisplay();
                updateNextPreviousButton();
            }
        }

        private void updateNextPreviousButton()
        {
            if (pageIndex >= numOfPage)
            {
                nextBut.Image = global::ACPAddIn.Properties.Resources.nextDisabled;
                this.nextBut.Cursor = System.Windows.Forms.Cursors.Default;
            }
            else if(!isHoverNext)
            {
                nextBut.Image = global::ACPAddIn.Properties.Resources.next;
                this.nextBut.Cursor = System.Windows.Forms.Cursors.Hand;
            }

            if (pageIndex <= 1)
            {
                previousBut.Image = global::ACPAddIn.Properties.Resources.previousDisabled;
                this.previousBut.Cursor = System.Windows.Forms.Cursors.Default;
            }
            else if (!isHoverPrevious)
            {
                previousBut.Image = global::ACPAddIn.Properties.Resources.previous;
                this.previousBut.Cursor = System.Windows.Forms.Cursors.Hand;
            }
        }

        private void updateSuggestionPageDisplay()
        {
            int index = listBox1.SelectedIndex;

            int entriesInView = Globals.ThisAddIn.EntriesInView;
            
            suggestionSizeLabel.Text = pageIndex + " of " + numOfPage;

            // remove previous entries
            listBox1.Items.Clear();
            displaySuggestion.Clear();

            int entryIndex = 1;
            for (int i = (pageIndex-1)*entriesInView, j = entriesInView;
                i < suggestions.Count && j > 0;
                i++, j--)
            {
                listBox1.Items.Add(suggestions[i]);
                displaySuggestion.Add(suggestions[i]);

                entryIndex++;
            }

            ListBox_initWidth(listBox1);

            if (index < listBox1.Items.Count)
            {
                listBox1.SelectedIndex = index;
            }
            else
            {
                listBox1.SelectedIndex = listBox1.Items.Count - 1;
            }
        }

        public int getPageIndex()
        {
            return pageIndex;
        }

        public int getNumOfPage()
        {
            return numOfPage;
        }

        public void selectFirstIndex()
        {
            listBox1.SelectedIndex = 0;
            if (numOfPage > 1)
            {
                statusLabel.Text = "Press Left/Right to navigate pages. Press Escape to hide.";
            }
            else
            {
                statusLabel.Text = "Press Up/Down to navigate suggestions. Press Escape to hide.";
            }
            this.Focus();
        }
        #endregion

        #region Quick Pasting
        public void quickPaste(int i)
        {
            Globals.ThisAddIn.insertSuggestion(displaySuggestion[i]);
        }

        public int getDisplaySuggestionCount()
        {
            return displaySuggestion.Count();
        }
        #endregion

        #region Text Wrapping
        public String getSourceNameWrapping(String sourceName, Font font)
        {
            int gap = 25;
            String[] words = sourceName.Split(' ');
            int width = getRenderTextWidth(sourceName, listBox1, font);
            String result = "";

            if (width > listBox1.Width - gap)
            {
                result += words[0];
                width = getRenderTextWidth(result, listBox1, font);

                // check if the first word width is larger than the listbox1 width
                if (width > listBox1.Width - gap)
                {
                    while (getRenderTextWidth(result + " ...", listBox1, font) > listBox1.Width - gap)
                    {
                        result = result.Remove(result.Length - 1, 1);
                    }
                }
                else
                {
                    for (int i = 1; i < words.Count(); i++)
                    {
                        String tempResult = result + " " + words[i];
                        width = getRenderTextWidth(tempResult + " ...", listBox1, font);

                        if (width > listBox1.Width - gap)
                        {
                            break;
                        }
                        else
                        {
                            result = tempResult;
                        }
                    }
                }

                result = result + " ...";
            }
            else
            {
                result = sourceName;
            }

            return result;
        }

        public String getSuggestionWrapping(String suggestion, Font font, bool entityMode)
        {
            int gap = 30;
            if (entityMode)
            {
                gap = 100;
            }
            String[] words = suggestion.Split(' ');
            String result = "";

            int width = getRenderTextWidth(suggestion, listBox1, font);

            if (width > listBox1.Width - gap)
            {

                ArrayList wordIndexCollection = new ArrayList();

                // The check sequence define what kind of substring pattern we wish to have
                // So by having the following value {0, words.length, 1, 2}
                // It will check the following substring accordingly:
                // Example: "This is an example of substring sequence check."
                // "This ... "
                // "This ... check."
                // "This is ... check."
                // "This is an ... check."
                // This is an absolute lame and stupid check.
                int[] firstCheckSequence = { 0, words.Count()-1, 1, 2 }; 

                for(int i=0; i<firstCheckSequence.Count(); i++){
                    wordIndexCollection.Add(firstCheckSequence[i]);
                    result = generateSubstring(words, wordIndexCollection);
                    width = getRenderTextWidth(result, listBox1, font);

                    // check if the width is wider
                    if (width > listBox1.Width - gap)
                    {
                        if (i == 0)
                        {
                            // If the first word is longer than the width list, we need to substring by characters
                            while(getRenderTextWidth(
                                generateSubstring(words, wordIndexCollection), 
                                listBox1, font) > listBox1.Width - gap){
                                    words[0] = words[0].Remove(words[0].Length - 1, 1);
                            }
                            result = generateSubstring(words, wordIndexCollection);
                        }
                        break;
                    }
                }
                
                // After having the initial pattern check, we will try to take as many words into 
                // the substring as possible based on the length
                // cStart and cEnd define the start and end indexes to perform this operation.
                int cStart = 3, cEnd = words.Count()-2;

                while (width < listBox1.Width - gap)
                {
                    String cStartWord = words[cStart];
                    String cEndWord = words[cEnd];
                    String tempResult;

                    if (cStartWord.Length < cEndWord.Length)
                    {
                        wordIndexCollection.Add(cStart);
                        cStart++;
                    }
                    else
                    {
                        wordIndexCollection.Add(cEnd);
                        cEnd--;
                    }

                    tempResult = generateSubstring(words, wordIndexCollection);
                    width = getRenderTextWidth(tempResult, listBox1, font);

                    // check if the width is wider
                    if (width > listBox1.Width - gap)
                    {
                        break;
                    }
                    else
                    {
                        result = tempResult;
                    }
                }
            }
            else{
                result = suggestion;
            }

            return result;
        }

        private int getRenderTextWidth(String text, Control control, Font font)
        {
            Graphics g = control.CreateGraphics();
            int width = (int)g.MeasureString(text, font).Width;

            g.Dispose();
            return width;
        }

        private int getRenderTextHeight(String text, Control control, Font font)
        {
            Graphics g = control.CreateGraphics();
            int height = (int)g.MeasureString(text, font).Height;

            g.Dispose();
            return height;
        }

        // Generate the substring from the list of choosen words
        private String generateSubstring(String[] words, ArrayList wordIndexes)
        {
            wordIndexes.Sort();
            object[] arrayWordIndexes = wordIndexes.ToArray();
            
            // Special case
            if (wordIndexes.Count == 1)
            {
                return words[0] + " ...";
            }

            String result = "";

            try
            {
                for (int i = 0; i < arrayWordIndexes.Count(); i++)
                {
                    int wordIndex = (int)arrayWordIndexes[i];

                    if (i == 0)
                    {
                        // first word
                        result += words[wordIndex];
                    }
                    else
                    {
                        result += " ";
                        int prevWordIndex = (int)arrayWordIndexes[i - 1];

                        if (wordIndex - prevWordIndex != 1)
                        {
                            result += "... " + words[wordIndex];
                        }
                        else
                        {
                            result += words[wordIndex];
                        }
                    }
                }
            }
            catch (Exception e)
            {
                return "";
            }

            return result;
        }
        #endregion

        #region Listbox Customization
        private void ListBox1_DrawItem(object sender, DrawItemEventArgs e)
        {
            bool selected = false;

            if ((e.State & DrawItemState.Selected) == DrawItemState.Selected)
            {
                e = new DrawItemEventArgs(e.Graphics,
                                          e.Font,
                                          e.Bounds,
                                          e.Index,
                                          e.State ^ DrawItemState.Selected,
                                          e.ForeColor,
                                          Color.FromArgb(51, 153, 255));//Choose the color
                selected = true;
                // Draw the selected background
                e.DrawBackground();
            }
            else
            {
                if (e.Index % 2 == 1)
                {
                    e.Graphics.FillRectangle(new SolidBrush(Color.FromArgb(239, 248, 253)), e.Bounds);
                }
                else
                {
                    // Draw the default background
                    e.DrawBackground();
                }
            }

            // If the ListBox has focus, draw a focus rectangle around the selected item.
            e.DrawFocusRectangle();

            if (e.Index >= 0)
            {
                Suggestion suggestion = (Suggestion)listBox1.Items[e.Index];
                String content="", sourceName="";
                bool entityMode = false;
                switch(suggestion.type){
                    case Suggestion.ENTITY:
                        Entity entity = (Entity)suggestion;
                        content = entity.content;
                        sourceName = entity.source.name;
                        entityMode = true;
                        break;
                    case Suggestion.SENTENCE:
                        Sentence sentence = (Sentence)suggestion;
                        content = sentence.content;
                        sourceName = sentence.source.name;
                        break;
                }

                content = content.Replace("\n", " ");

                Rectangle drawBound = e.Bounds;
                drawBound.X += padding;
                drawBound.Y += padding;
                drawBound.Width -= padding * 2;
                drawBound.Height -= padding * 2;

                StringFormat stringFormat = StringFormat.GenericDefault;
                stringFormat.Alignment = StringAlignment.Near;

                // Draw Index String
                Brush myBrush = new SolidBrush(Color.FromArgb(102, 106, 114));
                if (selected)
                {
                    myBrush = Brushes.White;
                }
                Font font = new Font(e.Font.FontFamily, e.Font.Size + 6, e.Font.Style);
                drawBound.Y += 2;
                e.Graphics.DrawString((e.Index + 1) + "", font, myBrush, drawBound, stringFormat);
                drawBound.Y -= 2;

                // Draw Content String
                myBrush = Brushes.Black;
                if (selected)
                {
                    myBrush = Brushes.White;
                }
                font = new Font(e.Font.FontFamily, e.Font.Size + 1, e.Font.Style);
                content = getSuggestionWrapping(content, font, entityMode);
                drawBound.X += 18;
                drawBound.Width -= 18;

                e.Graphics.DrawString(content, font, myBrush, drawBound, stringFormat);

                if (suggestion.type == Suggestion.ENTITY)
                {
                    int saveX = drawBound.X;
                    int saveWidth = drawBound.Width;

                    Entity entity = (Entity)suggestion;

                    Image icon = null;
                    int iconWidth=0, iconHeight=0; // Have to manually set the icon size to its actual size.
                    int offsetY = 0, offsetX = 0; 
                    String entityTypeName = "";

                    switch (entity.entityType)
                    {
                        case Entity.EMAIL:
                            entityTypeName = "Email";
                            icon = global::ACPAddIn.Properties.Resources.email_icon;
                            iconWidth = 16;
                            iconHeight = 10;
                            offsetY = 2;
                            offsetX = -5;
                            break;
                        case Entity.URL:
                            entityTypeName = "Link";
                            icon = global::ACPAddIn.Properties.Resources.link_icon;
                            iconWidth = 15;
                            iconHeight = 6;
                            offsetY = 4;
                            offsetX = -5;
                            break;
                        case Entity.PLACE:
                            entityTypeName = "Location";
                            icon = global::ACPAddIn.Properties.Resources.place_icon;
                            iconWidth = 8;
                            iconHeight = 13;
                            offsetY = 0;
                            offsetX = -1;
                            break;
                        case Entity.NAME:
                            entityTypeName = "Name";
                            icon = global::ACPAddIn.Properties.Resources.name_icon;
                            iconWidth = 16;
                            iconHeight = 13;
                            offsetY = 0;
                            offsetX = -5;
                            break;
                    }

                    drawBound.X += 240;
                    drawBound.Width -= 240;

                    e.Graphics.DrawImage(icon, new Rectangle(drawBound.X + offsetX, drawBound.Y + offsetY, iconWidth, iconHeight));
                    drawBound.X += 12;

                    e.Graphics.DrawString(entityTypeName, e.Font, myBrush, drawBound, stringFormat);
                    
                    drawBound.X = saveX;
                    drawBound.Width = saveWidth;
               }

                drawBound.Y += getRenderTextHeight(content, listBox1, font) ;
                drawBound.Height -= getRenderTextHeight(content, listBox1, font) ;

                myBrush = new SolidBrush(Color.FromArgb(54, 96, 137));
                font = new Font(e.Font.FontFamily, e.Font.Size - 1, e.Font.Style);
                sourceName = getSourceNameWrapping(sourceName, font);
                if (selected)
                {
                    myBrush = Brushes.White;
                }

                e.Graphics.DrawString(sourceName, font, myBrush, drawBound, StringFormat.GenericDefault);

                // Draw bottom line if it is not the last entry
                if (e.Index != listBox1.Items.Count-1)
                {
                    Pen pen = new Pen(Color.FromArgb(185,187,189), 1);
                    Point point1 = new Point(0, e.Bounds.Y + e.Bounds.Height - 1);
                    Point point2 = new Point(e.Bounds.X + e.Bounds.Width - 1, e.Bounds.Y + e.Bounds.Height - 1);
                    e.Graphics.DrawLine(pen, point1, point2);
                }
            }

            e.DrawFocusRectangle();
        }

        private void ListBox_initWidth(ListBox listBox)
        {
            int width = 0;
            Graphics g = listBox.CreateGraphics();

            foreach (object item in listBox.Items)
            {
                string text = item.ToString();
                SizeF s = g.MeasureString(text, listBox.Font);
                if (s.Width > width)
                    width = (int)s.Width;
            }
            listBox.HorizontalExtent = width + 2;
        }
        #endregion

        #region Listbox Mouse and Key Events

        bool controlKeyPressed = false; // true if the control key is pressed
        bool enterKeyPressed = true;
        private void listBox1_KeyDown(object sender, KeyEventArgs e)
        {
            controlKeyPressed = false;

            if (ModifierKeys == Keys.Control)
            {
                // Set control key to true to allow listBox1_keyPressed to know that 
                // control key is been pressed. This is to prevent control + <key> 
                // to be sent back to Microsoft Word. 
                // Eg. Pressing CTRL + N while on the listBox will not pass CTRL + N to 
                // Microsoft Word.
                controlKeyPressed = true;

                Keys[] keys = {Keys.D1, Keys.D2, Keys.D3, Keys.D4, Keys.D5,
                                 Keys.D6, Keys.D7, Keys.D8, Keys.D9};

                for (int i = 0; i < this.getDisplaySuggestionCount(); i++)
                {
                    if (e.KeyCode == keys[i])
                    {
                        quickPaste(i);
                        e.Handled = true;
                    }
                }
            }
            else if (e.KeyCode == Keys.Left)
            {
                e.Handled = true;
                previousSuggestionPage();
            }
            else if (e.KeyCode == Keys.Right)
            {
                e.Handled = true;
                nextSuggestionPage();
            }
            else if (e.KeyCode == Keys.Enter)
            {
                Globals.ThisAddIn.insertSuggestion(displaySuggestion[listBox1.SelectedIndex]);
            }
            else if (e.KeyCode == Keys.Escape)
            {
                this.Hide();
            }
            else
            {
                String result = "";

                char key = (char)e.KeyCode;

                if (e.KeyCode == Keys.Tab)
                {
                    result = "{tab}";
                }

                if (!result.Equals(""))
                {
                    Globals.ThisAddIn.Application.Activate();
                    SendKeys.Send(result);
                    this.Hide();
                    e.Handled = true;
                }
            }  
        }

        private void listBox1_KeyPress(object sender, KeyPressEventArgs e)
        {
            // If the control key is not pressed, pass the key pressed to the Microsoft Word Document
            bool isEnterKeyPressed = (e.KeyChar == (char)Keys.Return);
            if (!controlKeyPressed && !isEnterKeyPressed)
            {
                String result = "";

                char key = e.KeyChar;

                if (char.IsLetter(key))
                {
                    result = key.ToString().ToLower();
                }
                else
                {
                    result = key.ToString();
                }

                Globals.ThisAddIn.Application.Activate();
                SendKeys.Send(result);
                this.Hide();
            }
            e.Handled = true;
        }

        private void ListBox1_SelectedIndexChanged(object sender,
        System.EventArgs e)
        {
            if (listBox1.SelectedIndex >= 0)
            {
                previewPanel.Location = new Point(this.Location.X + this.Width - 4, this.Location.Y + 16);
                previewPanel.Visible = true;

                Suggestion currentSuggestion = displaySuggestion[listBox1.SelectedIndex];
                switch (currentSuggestion.type)
                {
                    case Suggestion.SENTENCE:
                        previewPanel.setText(((Sentence)displaySuggestion[listBox1.SelectedIndex]).content);
                        break;
                    case Suggestion.ENTITY:
                        previewPanel.setText(((Entity)displaySuggestion[listBox1.SelectedIndex]).content);
                        break;
                }
                
            }
            else
            {
                previewPanel.Visible = false;
            }
            
        }

        private void listBox1_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            int index = this.listBox1.IndexFromPoint(e.Location);

            if (index != System.Windows.Forms.ListBox.NoMatches)
            {
                Globals.ThisAddIn.insertSuggestion(displaySuggestion[listBox1.SelectedIndex]);
            }
        }
        #endregion

        #region Mouse Event for Next/Previous/Exit Buttons
        // Mouse Events for Next and Previous Buttons
        private void nextBut_MouseHover(object sender, EventArgs e)
        {
            if (pageIndex < numOfPage)
            {
                nextBut.Image = global::ACPAddIn.Properties.Resources.nextHover;
            }
            isHoverNext = true;
        }

        private void nextBut_MouseLeave(object sender, EventArgs e)
        {
            if (pageIndex < numOfPage)
            {
                nextBut.Image = global::ACPAddIn.Properties.Resources.next;
            }
            isHoverNext = false;
        }

        private void nextBut_MouseClick(object sender, MouseEventArgs e)
        {
            if (pageIndex < numOfPage)
            {
                nextSuggestionPage();
            }
        }

        private void previousBut_MouseHover(object sender, EventArgs e)
        {
            if (pageIndex > 1)
            {
                previousBut.Image = global::ACPAddIn.Properties.Resources.previousHover;
            }
            isHoverPrevious = true;
        }

        private void previousBut_MouseLeave(object sender, EventArgs e)
        {
            if (pageIndex > 1)
            {
                previousBut.Image = global::ACPAddIn.Properties.Resources.previous;
            }
            isHoverPrevious = false;
        }

        private void previousBut_MouseClick(object sender, MouseEventArgs e)
        {
            if (pageIndex > 1)
            {
                previousSuggestionPage();
            }
        }

        private void closeBut_MouseHover(object sender, EventArgs e)
        {
            closeBut.Image = global::ACPAddIn.Properties.Resources.closeHover;
        }

        private void closeBut_MouseClick(object sender, MouseEventArgs e)
        {
            Hide();
        }

        private void closeBut_MouseLeave(object sender, EventArgs e)
        {
            closeBut.Image = global::ACPAddIn.Properties.Resources.close;
        }

        public void Hide()
        {
            base.Hide();

            previewPanel.Hide();
        }
        #endregion
    }
}
