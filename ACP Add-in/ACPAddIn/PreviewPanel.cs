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
    /// This is the presentation layer which show the content of the selected text in the auto-complete suggestion box.
    /// 
    /// Author: Ng Chin Hui
    /// </summary>
    public partial class PreviewPanel : Form
    {
        public PreviewPanel()
        {
            InitializeComponent();

            BackColor = Color.Lime;
            TransparencyKey = Color.Lime;
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
                baseParams.ClassStyle |= CS_DROPSHADOW;
                //baseParams.ExStyle |= (int)(
                //0x00000008);

                return baseParams;
            }
        }

        public void setText(String text)
        {
            previewLabel.Text = text;
        }
    }
}
