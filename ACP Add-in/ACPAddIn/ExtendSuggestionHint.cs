using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using ACPAddIn.Object_Class;
using IniParser;

namespace ACPAddIn
{
    /// <summary>
    /// This is the presentation layer for displaying a list of hotkeys available in extend suggestion mode.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    public partial class ExtendSuggestionHint : Form
    {
        private Timer fadeOutTimer;

        bool isShown = false;

        private Panel extendWordKeyPanel;
        private Panel reduceWordKeyPanel;
        private Panel extendSentenceKeyPanel;
        private Panel reduceSentenceKeyPanel;
        private Panel extendParagraphKeyPanel;
        private Panel reduceParagraphKeyPanel;

        public ExtendSuggestionHint()
        {
            InitializeComponent();

            BackColor = Color.Lime;
            TransparencyKey = Color.Lime;

            fadeOutTimer = new Timer();
            fadeOutTimer.Tick += new EventHandler(fadeOutEvent);
        }

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
                //baseParams.ClassStyle |= CS_DROPSHADOW;
                //baseParams.ExStyle |= (int)(
                //0x00000008);

                return baseParams;
            }
        }

        public void updateLocation(Point p, Size size)
        {
            this.Location = new Point((p.X + size.Width/2 - this.Width/2) , p.Y + size.Height - this.Height - 40);
        }

        public void fadeOut()
        {
            if (this.Visible)
            {
                fadeOutTimer.Stop();
                fadeOutTimer.Interval = 10;
                fadeOutTimer.Start();
            }
        }

        private void fadeOutEvent(Object myObject, EventArgs myEventArgs)
        {
            this.Opacity -= 0.05;
            if (this.Opacity <= .05)
            {
                this.Opacity = 0;
                this.Hide();
                fadeOutTimer.Stop();
            }
        }

        public void ShowForm()
        {
            base.Show();
            this.Opacity = 0.85;

            if (!isShown)
            {
                createHotkeyPanel();
                isShown = true;
            }
            else
            {
                updateHotkeyPanel();
            }
        }

        private void createHotkeyPanel()
        {
            IniData config = Globals.ThisAddIn.Configuration;
            
            String extendWordKey = config["hotkeys"]["extendWord"];
            extendWordKeyPanel = new Panel();
            setHotkeyPanelWithKeys(extendWordKeyPanel, extendWordKey);
            extendWordKeyPanel.Location = new Point( (extendWordPanel.Width-extendWordKeyPanel.Width)/2 , 18);
            extendWordPanel.Controls.Add(extendWordKeyPanel);

            String reduceWordKey = config["hotkeys"]["reduceWord"];
            reduceWordKeyPanel = new Panel();
            setHotkeyPanelWithKeys(reduceWordKeyPanel, reduceWordKey);
            reduceWordKeyPanel.Location = new Point((reduceWordPanel.Width - reduceWordKeyPanel.Width) / 2, 18);
            reduceWordPanel.Controls.Add(reduceWordKeyPanel);

            String extendSentenceKey = config["hotkeys"]["extendSentence"];
            extendSentenceKeyPanel = new Panel();
            setHotkeyPanelWithKeys(extendSentenceKeyPanel, extendSentenceKey);
            extendSentenceKeyPanel.Location = new Point((extendSentencePanel.Width - extendSentenceKeyPanel.Width) / 2, 18);
            extendSentencePanel.Controls.Add(extendSentenceKeyPanel);

            String reduceSentenceKey = config["hotkeys"]["reduceSentence"];
            reduceSentenceKeyPanel = new Panel();
            setHotkeyPanelWithKeys(reduceSentenceKeyPanel, reduceSentenceKey);
            reduceSentenceKeyPanel.Location = new Point((reduceSentencePanel.Width - reduceSentenceKeyPanel.Width) / 2, 18);
            reduceSentencePanel.Controls.Add(reduceSentenceKeyPanel);

            String extendParagraphKey = config["hotkeys"]["extendParagraph"];
            extendParagraphKeyPanel = new Panel();
            setHotkeyPanelWithKeys(extendParagraphKeyPanel, extendParagraphKey);
            extendParagraphKeyPanel.Location = new Point((extendParagraphPanel.Width - extendParagraphKeyPanel.Width) / 2, 18);
            extendParagraphPanel.Controls.Add(extendParagraphKeyPanel);

            String reduceParagraphKey = config["hotkeys"]["reduceParagraph"];
            reduceParagraphKeyPanel = new Panel();
            setHotkeyPanelWithKeys(reduceParagraphKeyPanel, reduceParagraphKey);
            reduceParagraphKeyPanel.Location = new Point((reduceParagraphPanel.Width - reduceParagraphKeyPanel.Width) / 2, 18);
            reduceParagraphPanel.Controls.Add(reduceParagraphKeyPanel);
        }

        private void updateHotkeyPanel()
        {
            IniData config = Globals.ThisAddIn.Configuration;

            String extendWordKey = config["hotkeys"]["extendWord"];
            setHotkeyPanelWithKeys(extendWordKeyPanel, extendWordKey);
            extendWordKeyPanel.Location = new Point((extendWordPanel.Width - extendWordKeyPanel.Width) / 2, 18);

            String reduceWordKey = config["hotkeys"]["reduceWord"];
            setHotkeyPanelWithKeys(reduceWordKeyPanel, reduceWordKey);
            reduceWordKeyPanel.Location = new Point((reduceWordPanel.Width - reduceWordKeyPanel.Width) / 2, 18);

            String extendSentenceKey = config["hotkeys"]["extendSentence"];
            setHotkeyPanelWithKeys(extendSentenceKeyPanel, extendSentenceKey);
            extendSentenceKeyPanel.Location = new Point((extendSentencePanel.Width - extendSentenceKeyPanel.Width) / 2, 18);

            String reduceSentenceKey = config["hotkeys"]["reduceSentence"];
            setHotkeyPanelWithKeys(reduceSentenceKeyPanel, reduceSentenceKey);
            reduceSentenceKeyPanel.Location = new Point((reduceSentencePanel.Width - reduceSentenceKeyPanel.Width) / 2, 18);

            String extendParagraphKey = config["hotkeys"]["extendParagraph"];
            setHotkeyPanelWithKeys(extendParagraphKeyPanel, extendParagraphKey);
            extendParagraphKeyPanel.Location = new Point((extendParagraphPanel.Width - extendParagraphKeyPanel.Width) / 2, 18);

            String reduceParagraphKey = config["hotkeys"]["reduceParagraph"];
            setHotkeyPanelWithKeys(reduceParagraphKeyPanel, reduceParagraphKey);
            reduceParagraphKeyPanel.Location = new Point((reduceParagraphPanel.Width - reduceParagraphKeyPanel.Width) / 2, 18);
        }

        private void setHotkeyPanelWithKeys(Panel panel, String keys)
        {
            panel.Controls.Clear();

            String[] keyList = keys.Split(' ');

            int x = 0;
            int gap = 2;

            for (int i = 0; i < keyList.Length; i++)
            {
                Label label = getKeyLabel(keyList[i]);
                label.Location = new Point(x, 0);
                panel.Controls.Add(label);

                x += label.Width + gap;
            }

            panel.Size = new Size(x, 27);

        }

        private Label getKeyLabel(String key)
        {
            Label newLabel = new Label();
            Boolean isText = true;

            if (!key.Equals("+"))
            {
                newLabel.BackColor = System.Drawing.Color.White;
                newLabel.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;

                switch (key)
                {
                    case "UP":
                        newLabel.Image = global::ACPAddIn.Properties.Resources.up;
                        isText = false;
                        break;
                    case "DOWN":
                        newLabel.Image = global::ACPAddIn.Properties.Resources.down;
                        isText = false;
                        break;
                    case "LEFT":
                        newLabel.Image = global::ACPAddIn.Properties.Resources.left;
                        isText = false;
                        break;
                    case "RIGHT":
                        newLabel.Image = global::ACPAddIn.Properties.Resources.right;
                        isText = false;
                        break;
                    case "COMMA":
                        newLabel.Text = "<";
                        break;
                    case "PERIOD":
                        newLabel.Text = ">";
                        break;
                    default:
                        newLabel.Text = key;
                        break;
                }

                int width=22;

                if (isText)
                {
                    Graphics g = newLabel.CreateGraphics();
                    width = (int)g.MeasureString(newLabel.Text, newLabel.Font).Width;
                    g.Dispose();
                    width += 5;
                }

                // Make the label a square if the width is too small
                if (width < 26)
                {
                    width = 26;
                }

                newLabel.Size = new System.Drawing.Size(width, 26);

                           
                newLabel.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            }
            else
            {
                newLabel.Size = new System.Drawing.Size(10, 26);
                newLabel.Text = key;
                newLabel.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            }

            return newLabel;
        }
    }
}
