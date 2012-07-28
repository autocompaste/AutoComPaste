namespace ACPTest
{
    using System;
    using System.Collections.Generic;
    using System.Drawing;
    using System.Windows.Input;
    using System.CodeDom.Compiler;
    using System.Text.RegularExpressions;
    using Microsoft.VisualStudio.TestTools.UITest.Extension;
    using Microsoft.VisualStudio.TestTools.UITesting;
    using Microsoft.VisualStudio.TestTools.UnitTesting;
    using Microsoft.VisualStudio.TestTools.UITesting.WinControls;
    using Keyboard = Microsoft.VisualStudio.TestTools.UITesting.Keyboard;
    using Mouse = Microsoft.VisualStudio.TestTools.UITesting.Mouse;
    using MouseButtons = System.Windows.Forms.MouseButtons;
    using System.IO;
    
    
    public partial class UIMap
    {
        private int waitingDelay = 1500;
        private int extendDelay = 800;

        public void PasteSentence()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "Jobs{space}was{space}born");

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys("{DOWN}{ENTER}");
        }

        public void PasteSentenceWithQuickSelect()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "When{space}it{space}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{1}", ModifierKeys.Control);
        }

        public void ExtendParagraphAfterPasting()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "His{space}adoptive{space}parents");

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(extendDelay);

            Keyboard.SendKeys("{DOWN}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{DOWN}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{DOWN}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{UP}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{UP}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{DOWN}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
        }

        public void ExtendSentenceAfterPasting()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "In{space}2003,");

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(extendDelay);

            Keyboard.SendKeys("{RIGHT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{RIGHT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{RIGHT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{LEFT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{RIGHT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
        }

        public void TriggerEnableHotKey()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys("D", ModifierKeys.Control);
            Playback.Wait(3000);
        }

        public void ExtendWordAfterPasting()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "People{space}flocked");

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(extendDelay);

            Keyboard.SendKeys("{<}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{<}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
        }

        public void ExtendTypesCombinationAfterPasting()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "By{space}think");

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(extendDelay);

            // Extend paragraph once, Extend 2 words, Reduce 2 sentence and Extend 2 word
            Keyboard.SendKeys("{DOWN}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{LEFT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{LEFT}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
            Keyboard.SendKeys("{>}", ModifierKeys.Control);
            Playback.Wait(extendDelay);
        }

        public void HideAndManualTriggerPasting()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "By{space}think");

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys("{ESCAPE}");

            Playback.Wait(500);
            Keyboard.SendKeys("{F9}");
            Playback.Wait(500);
            Keyboard.SendKeys("{DOWN}{ENTER}");
        }

        public void EntitySearchForName()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "The{space}name{space}of{space}Steve{space}Job{space}biological{space}mother{space}is{space}schie");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{2}", ModifierKeys.Control);
        }

        public void EntitySearchForName2()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "Her{space}name{space}was{space}san");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{1}", ModifierKeys.Control);
        }

        public void EntitySearchForPlace()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "He{space}was{space}born{space}in{space}the{space}city{space}of{space}san");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{1}", ModifierKeys.Control);
        }

        public void EntitySearchForEmail()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "Steve{space}job{space}email{space}was{space}sjobs");

            Playback.Wait(500);
            Keyboard.SendKeys("{F9}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{1}", ModifierKeys.Control);
        }

        public void EntitySearchForWebpage()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "To{space}find{space}out{space}more{space}about{space}the{space}specification{space}of{space}iPhone,{space}please{space}visit{space}the{space}page{space}at{space}specs");

            Playback.Wait(500);
            Keyboard.SendKeys("{F9}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");
        }

        public void RankingSubsequentSentences()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "While{space}the{space}ceo");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{ENTER}{ENTER}");
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "Jobs{space}was{space}born{space}in");
            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{ENTER}{ENTER}");
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "After{space}");
            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{DOWN}{ENTER}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{ENTER}{ENTER}");
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "After{space}");
            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");
        }

        public void RankingByFrequency()
        {
            WinClient uIMicrosoftWordDocumenClient = this.UIDocument1MicrosoftWoWindow.UIDocument1Client.UIMicrosoftWordDocumenClient;
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "People{space}flocked");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{ENTER}{ENTER}");
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "In{space}reality,");
            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");

            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{ENTER}{ENTER}");
            Keyboard.SendKeys(uIMicrosoftWordDocumenClient, "That{space}");
            Playback.Wait(waitingDelay);
            Keyboard.SendKeys("{DOWN}{ENTER}");
        }

        public void SaveDocument(String fileName)
        {
            Keyboard.SendKeys("S", ModifierKeys.Control);

            Playback.Wait(waitingDelay);

            Keyboard.SendKeys(fileName);
            Keyboard.SendKeys("{TAB}");

            for (int i = 0; i < 13; i++)
            {
                Keyboard.SendKeys("{DOWN}");
            }

            Keyboard.SendKeys("{ENTER}");

            Playback.Wait(200);

            Keyboard.SendKeys("{ENTER}");

            Playback.Wait(200);

            Keyboard.SendKeys("{ENTER}");
            Playback.Wait(200);
            Keyboard.SendKeys("{ENTER}");
        }

        public void ExitWindow()
        {
            Keyboard.SendKeys("{F4}", ModifierKeys.Alt);
            Playback.Wait(100);
            Keyboard.SendKeys("{RIGHT}{ENTER}");
        }

        public String readFile(String filename)
        {
           return File.ReadAllText(filename);
        }
    }
}
