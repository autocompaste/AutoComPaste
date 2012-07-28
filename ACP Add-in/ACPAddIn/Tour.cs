using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Collections;
using IniParser;

namespace ACPAddIn
{
    public partial class Tour : Form
    {
        private static ArrayList tourScreen;
        private IniData config;
        private String configFilePath;
        private int currentScreen;
        public Tour(IniData config, String configFilePath)
        {
            InitializeComponent();
            this.config = config;
            this.configFilePath = configFilePath;
            tourScreen = new ArrayList();
            initializeTourScreenArray();
            currentScreen = 0;
            showCurrentScreen();
            setButtonStates();
            setLabels();
        }

        private void initializeTourScreenArray()
        {
            tourScreen.Add(this.tourScreen0);
            tourScreen.Add(this.TourScreen1);
            tourScreen.Add(this.TourScreen2);
            tourScreen.Add(this.tourScreen3);
            tourScreen.Add(this.tourScreen4);
        }

        private void setLabels()
        {
            this.extend_word_lbl.Text = config["hotkeys"]["extendWord"];
            this.reduce_word_lbl.Text = config["hotkeys"]["reduceWord"];
            this.extend_sentence_lbl.Text = config["hotkeys"]["extendSentence"];
            this.reduce_sentence_lbl.Text = config["hotkeys"]["reduceSentence"];
            this.extend_para_lbl.Text = config["hotkeys"]["extendParagraph"];
            this.reduce_para_lbl.Text = config["hotkeys"]["reduceParagraph"];
        }

        private void setButtonStates()
        {
            //reset button visibility
            this.back_btn.Visible = true;
            this.next_btn.Visible = true;
            this.finish_btn.Visible = false;

            //hide the state based on the current screen.
            if (currentScreen == 0)
            {
                this.back_btn.Visible = false;
            }
            if (currentScreen == tourScreen.Count - 1)
            {
                this.next_btn.Visible = false;
                this.finish_btn.Visible = true;
            }
        }

        private void gotoNextScreen()
        {
            currentScreen++;
            showCurrentScreen();
            setButtonStates();
            
        }

        private void gotoPreviousScreen()
        {
            currentScreen--;
            showCurrentScreen();
            setButtonStates();
        }

        private void showCurrentScreen()
        {
            foreach (PictureBox s in tourScreen)
            {
                s.Visible = false;
            }
            PictureBox screenToDisplay = (PictureBox)tourScreen[currentScreen];
            screenToDisplay.Visible = true;
            hotkeysVisibility();
        }

        private void hotkeysVisibility()
        {
            if (currentScreen == 3)
            {
                this.extend_para_lbl.Visible = true;
                this.extend_sentence_lbl.Visible = true;
                this.extend_word_lbl.Visible = true;
                this.reduce_word_lbl.Visible = true;
                this.reduce_sentence_lbl.Visible = true;
                this.reduce_para_lbl.Visible = true;
            }
            else
            {
                this.extend_para_lbl.Visible = false;
                this.extend_sentence_lbl.Visible = false;
                this.extend_word_lbl.Visible = false;
                this.reduce_word_lbl.Visible = false;
                this.reduce_sentence_lbl.Visible = false;
                this.reduce_para_lbl.Visible = false;
            }
        }

        private void next_btn_Click(object sender, EventArgs e)
        {
            gotoNextScreen();
        }

        private void back_btn_Click(object sender, EventArgs e)
        {
            gotoPreviousScreen();
        }

        private void finish_btn_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void Tour_FormClosed(object sender, FormClosedEventArgs e)
        {
            FileIniDataParser parser = new FileIniDataParser();
            this.config["settings"]["tourEnabled"] = (!this.checkBox1.Checked).ToString();
            parser.SaveFile(this.configFilePath, this.config);
        }
    }
}
