using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Windows.Input;
using System.Windows.Forms;
using System.Drawing;
using Microsoft.VisualStudio.TestTools.UITesting;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Microsoft.VisualStudio.TestTools.UITest.Extension;
using Keyboard = Microsoft.VisualStudio.TestTools.UITesting.Keyboard;
using System.IO;


namespace ACPTest
{
    /// <summary>
    /// Summary description for UITesting
    /// </summary>
    [CodedUITest]
    public class UITesting
    {
        String directory = "C:\\Windows\\Temp\\";
        String fileName = "acp-test";

        public UITesting()
        {
            
        }

        [TestMethod]
        public void PasteSentence()
        {        
            this.UIMap.LaunchMicrosoftWord();

            UIMap.PasteSentence();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "Jobs was born in San Francisco on February 24, 1955.";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void PasteSentenceWithQuickSelect()
        {
            this.UIMap.LaunchMicrosoftWord();

            UIMap.PasteSentenceWithQuickSelect();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "When it was announced that he'd be stepping down, analysts worried that the company would flounder without him.";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        // Extend paragraph once, Extend 2 words, Reduce 2 sentence and Extend 2 words
        public void HideAndManualTriggerPasting()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.HideAndManualTriggerPasting();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "By thinking differently, Jobs placed himself squarely in the mainstream.";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        // Extend paragraph once, Extend 2 words, Reduce 2 sentence and Extend 2 words
        public void EnableAndDisableACP()
        {
            this.UIMap.LaunchMicrosoftWord();

            UIMap.TriggerEnableHotKey();
            UIMap.PasteSentence();
            UIMap.TriggerEnableHotKey();
            UIMap.PasteSentence();

            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "Jobs was born\r\nJobs was born in San Francisco on February 24, 1955.";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void ExtendParagraphAfterPasting()
        {
            this.UIMap.LaunchMicrosoftWord();

            UIMap.ExtendParagraphAfterPasting();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "His adoptive parents, Paul and Clara Jobs, were Armenian and unable to have children. Steve was later joined in the family by his adopted sister Patti Jobs, born in 1958. The couple divorced in 1962. \r\n\r\nThough Steve did not know until much later, Abdulfattah Jandali later married Joanne Schieble and had another child, Mona, in 1957, whom they kept. Steve Jobs discovered he had a biological sister, the successful novelist Mona Simpson, at the age of 27. ";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void ExtendSentenceAfterPasting()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.ExtendSentenceAfterPasting();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "In 2003, Jobs was diagnosed with a pancreas neuroendocrine tumor. Though it was initially treated, he reported a hormone imbalance, underwent a liver transplant in 2009, and appeared progressively thinner as his health declined. On medical leave for most of 2011, Jobs resigned as Apple CEO in August that year and was elected Chairman of the Board. He died of respiratory arrest related to his metastatic tumor on October 5, 2011.";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void ExtendWordAfterPasting()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.ExtendWordAfterPasting();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "People flocked to Apple stores across the globe to leave flowers. Groups used the candle";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        // Extend paragraph once, Extend 2 words, Reduce 2 sentence and Extend 2 words
        public void ExtendTypesCombinationAfterPasting()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.ExtendTypesCombinationAfterPasting();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "By thinking differently, Jobs placed himself squarely in the mainstream. With the invention of the iPod and iPhone, Apple went from a quirky underdog to a global powerhouse. Its ubiquitous";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void EntitySearchForName()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.EntitySearchForName();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "The name of Steve Job biological mother is Joanne Schieble";
            String expected2 = "The name of steve job biological mother is Schieble";
            File.Delete(directory + fileName + ".txt");

            bool result = false;

            if (output.Trim().ToLower().Equals(expected.Trim().ToLower())
                || output.Trim().ToLower().Equals(expected2.Trim().ToLower()))
            {
                result = true;
            }

            Assert.IsTrue(result);
        }

        [TestMethod]
        public void EntitySearchForName2()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.EntitySearchForName2();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "Her name was Susan Sarandon";
            String expected2 = "Her name was Susanna";
            File.Delete(directory + fileName + ".txt");

            bool result = false;

            if (output.Trim().ToLower().Equals(expected.Trim().ToLower())
                || output.Trim().ToLower().Equals(expected2.Trim().ToLower()))
            {
                result = true;
            }

            Assert.IsTrue(result);
        }

        [TestMethod]
        public void EntitySearchForPlace()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.EntitySearchForPlace();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "He was born in the city of San Francisco";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void EntitySearchForEmail()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.EntitySearchForEmail();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "Steve job email was sjobs@apple.com";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void EntitySearchForWebpage()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.EntitySearchForWebpage();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "To find out more about the specification of iPhone, please visit the page at http://www.apple.com/iphone/iphone-4/specs.html";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void RankingByFrequency()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.RankingByFrequency();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "People flocked to Apple stores across the globe to leave flowers. \r\nIn reality, they knew very little about him. \r\nThat Jobs never revealed much about his politics or his personal life also meant that he could never disappoint fans' preconceived notions.";
            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }

        [TestMethod]
        public void RankingSubsequentSentences()
        {
            this.UIMap.LaunchMicrosoftWord();
            UIMap.RankingSubsequentSentences();
            UIMap.SaveDocument(directory + fileName);
            UIMap.ExitWindow();

            String output = UIMap.readFile(directory + fileName + ".txt");
            String expected = "While the CEO and co-founder of Apple steered most interviews away from the public fascination with his private life, there's plenty we know about Jobs the person, beyond the Mac and the iPhone. \r\nJobs was born in San Francisco on February 24, 1955. \r\nAfter reuniting, Jobs and Simpson developed a close relationship. \r\nAfter later mending their relationship, Jobs paid for his first daughter's education at Harvard.";

            File.Delete(directory + fileName + ".txt");

            Assert.AreEqual(output.Trim().ToLower(), expected.Trim().ToLower());
        }


        #region Additional test attributes

        // You can use the following additional attributes as you write your tests:

        ////Use TestInitialize to run code before running each test 
        //[TestInitialize()]
        //public void MyTestInitialize()
        //{        
        //    // To generate code for this test, select "Generate Code for Coded UI Test" from the shortcut menu and select one of the menu items.
        //    // For more information on generated code, see http://go.microsoft.com/fwlink/?LinkId=179463
        //}

        ////Use TestCleanup to run code after each test has run
        //[TestCleanup()]
        //public void MyTestCleanup()
        //{        
        //    // To generate code for this test, select "Generate Code for Coded UI Test" from the shortcut menu and select one of the menu items.
        //    // For more information on generated code, see http://go.microsoft.com/fwlink/?LinkId=179463
        //}

        #endregion

        /// <summary>
        ///Gets or sets the test context which provides
        ///information about and functionality for the current test run.
        ///</summary>
        public TestContext TestContext
        {
            get
            {
                return testContextInstance;
            }
            set
            {
                testContextInstance = value;
            }
        }
        private TestContext testContextInstance;

        public UIMap UIMap
        {
            get
            {
                if ((this.map == null))
                {
                    this.map = new UIMap();
                }

                return this.map;
            }
        }

        private UIMap map;
    }
}
