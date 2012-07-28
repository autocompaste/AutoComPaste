using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using ACPAddIn.Object_Class;

namespace ACPAddIn
{
    /// <summary>
    /// This is the presentation layer to display fade-in notification at the bottom-right corner
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    
    public partial class Notification : Form
    {
        private Timer fadeInTimer;
        private Timer timer;

        public Notification()
        {
            InitializeComponent();

            BackColor = Color.Lime;
            TransparencyKey = Color.Lime;
            fadeInTimer = new Timer();
            fadeInTimer.Tick += new EventHandler(FadeInEvent);
            timer = new Timer();
            timer.Tick += new EventHandler(HideEvent);            

            this.Opacity = 0;
        }

        protected override bool ShowWithoutActivation
        {
            get { return true; }
        }

        protected override CreateParams CreateParams
        {
            get
            {
                CreateParams baseParams = base.CreateParams;

                //baseParams.ExStyle |= (int)(
                  //0x00000008);

                return baseParams;
            }
        }

        public void setMessage(String message)
        {
            messageLabel.Text = message;
        }

        public void updateLocation(Point p, Size size)
        {
            this.Location = new Point(p.X + size.Width - this.Width - 30, p.Y + size.Height - this.Height - 32);
        }

        public void showWithTimer(int second)
        {
            Show();

            this.Opacity = 0;

            fadeInTimer.Stop();
            fadeInTimer.Interval = 18;            
            fadeInTimer.Start();

            timer.Stop();
            timer.Interval = second * 1000;
            timer.Start();
        }

        private void FadeInEvent(Object myObject, EventArgs myEventArgs)
        {
            this.Opacity += 0.05;
            if (this.Opacity >= .95)
            {
                this.Opacity = 1;
                fadeInTimer.Stop();
            }
        }

        private void HideEvent(Object myObject, EventArgs myEventArgs)
        {
            timer.Stop();
            this.Opacity = 0;
            Hide();
        }
    }
}
