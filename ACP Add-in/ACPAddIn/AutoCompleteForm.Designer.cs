namespace ACPAddIn
{
    partial class AutoCompleteForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(AutoCompleteForm));
            this.listBox1 = new System.Windows.Forms.ListBox();
            this.panel1 = new System.Windows.Forms.Panel();
            this.closeBut = new System.Windows.Forms.Label();
            this.previousBut = new System.Windows.Forms.Label();
            this.nextBut = new System.Windows.Forms.Label();
            this.suggestionSizeLabel = new System.Windows.Forms.Label();
            this.statusLabel = new System.Windows.Forms.Label();
            this.panel1.SuspendLayout();
            this.SuspendLayout();
            // 
            // listBox1
            // 
            this.listBox1.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.listBox1.FormattingEnabled = true;
            this.listBox1.Location = new System.Drawing.Point(0, 16);
            this.listBox1.Name = "listBox1";
            this.listBox1.Size = new System.Drawing.Size(330, 199);
            this.listBox1.TabIndex = 0;
            this.listBox1.KeyDown += new System.Windows.Forms.KeyEventHandler(this.listBox1_KeyDown);
            this.listBox1.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.listBox1_KeyPress);
            this.listBox1.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.listBox1_MouseDoubleClick);
            // 
            // panel1
            // 
            this.panel1.BackColor = System.Drawing.SystemColors.Control;
            this.panel1.BackgroundImage = ((System.Drawing.Image)(resources.GetObject("panel1.BackgroundImage")));
            this.panel1.Controls.Add(this.closeBut);
            this.panel1.Controls.Add(this.previousBut);
            this.panel1.Controls.Add(this.nextBut);
            this.panel1.Controls.Add(this.suggestionSizeLabel);
            this.panel1.Location = new System.Drawing.Point(0, 0);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(330, 16);
            this.panel1.TabIndex = 1;
            // 
            // closeBut
            // 
            this.closeBut.BackColor = System.Drawing.Color.Transparent;
            this.closeBut.Cursor = System.Windows.Forms.Cursors.Hand;
            this.closeBut.Image = global::ACPAddIn.Properties.Resources.close;
            this.closeBut.Location = new System.Drawing.Point(310, 2);
            this.closeBut.Name = "closeBut";
            this.closeBut.Size = new System.Drawing.Size(12, 12);
            this.closeBut.TabIndex = 3;
            this.closeBut.MouseClick += new System.Windows.Forms.MouseEventHandler(this.closeBut_MouseClick);
            this.closeBut.MouseLeave += new System.EventHandler(this.closeBut_MouseLeave);
            this.closeBut.MouseHover += new System.EventHandler(this.closeBut_MouseHover);
            // 
            // previousBut
            // 
            this.previousBut.BackColor = System.Drawing.Color.Transparent;
            this.previousBut.Cursor = System.Windows.Forms.Cursors.Hand;
            this.previousBut.Image = global::ACPAddIn.Properties.Resources.previous;
            this.previousBut.Location = new System.Drawing.Point(225, 2);
            this.previousBut.Name = "previousBut";
            this.previousBut.Size = new System.Drawing.Size(12, 12);
            this.previousBut.TabIndex = 2;
            this.previousBut.MouseClick += new System.Windows.Forms.MouseEventHandler(this.previousBut_MouseClick);
            this.previousBut.MouseLeave += new System.EventHandler(this.previousBut_MouseLeave);
            this.previousBut.MouseHover += new System.EventHandler(this.previousBut_MouseHover);
            // 
            // nextBut
            // 
            this.nextBut.BackColor = System.Drawing.Color.Transparent;
            this.nextBut.Cursor = System.Windows.Forms.Cursors.Hand;
            this.nextBut.Image = global::ACPAddIn.Properties.Resources.next;
            this.nextBut.Location = new System.Drawing.Point(243, 2);
            this.nextBut.Name = "nextBut";
            this.nextBut.Size = new System.Drawing.Size(12, 12);
            this.nextBut.TabIndex = 1;
            this.nextBut.MouseClick += new System.Windows.Forms.MouseEventHandler(this.nextBut_MouseClick);
            this.nextBut.MouseLeave += new System.EventHandler(this.nextBut_MouseLeave);
            this.nextBut.MouseHover += new System.EventHandler(this.nextBut_MouseHover);
            // 
            // suggestionSizeLabel
            // 
            this.suggestionSizeLabel.AutoSize = true;
            this.suggestionSizeLabel.BackColor = System.Drawing.Color.Transparent;
            this.suggestionSizeLabel.Cursor = System.Windows.Forms.Cursors.Arrow;
            this.suggestionSizeLabel.Location = new System.Drawing.Point(232, 1);
            this.suggestionSizeLabel.MinimumSize = new System.Drawing.Size(100, 0);
            this.suggestionSizeLabel.Name = "suggestionSizeLabel";
            this.suggestionSizeLabel.Size = new System.Drawing.Size(100, 13);
            this.suggestionSizeLabel.TabIndex = 0;
            this.suggestionSizeLabel.Text = "1 of 20";
            this.suggestionSizeLabel.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // statusLabel
            // 
            this.statusLabel.BackColor = System.Drawing.Color.WhiteSmoke;
            this.statusLabel.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.statusLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.statusLabel.Location = new System.Drawing.Point(0, 214);
            this.statusLabel.Name = "statusLabel";
            this.statusLabel.Padding = new System.Windows.Forms.Padding(1);
            this.statusLabel.Size = new System.Drawing.Size(330, 18);
            this.statusLabel.TabIndex = 2;
            this.statusLabel.Text = "Press CTRL+1 to select first entry. Press Escape to hide.";
            this.statusLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // AutoCompleteForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(332, 233);
            this.ControlBox = false;
            this.Controls.Add(this.listBox1);
            this.Controls.Add(this.statusLabel);
            this.Controls.Add(this.panel1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "AutoCompleteForm";
            this.ShowIcon = false;
            this.ShowInTaskbar = false;
            this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Hide;
            this.StartPosition = System.Windows.Forms.FormStartPosition.Manual;
            this.Text = "Form1";
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListBox listBox1;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Label suggestionSizeLabel;
        private System.Windows.Forms.Label statusLabel;
        private System.Windows.Forms.Label nextBut;
        private System.Windows.Forms.Label previousBut;
        private System.Windows.Forms.Label closeBut;
    }
}