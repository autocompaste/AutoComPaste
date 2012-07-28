/// <summary> This class is used for displaying tour to introduce the main functionality of ACP to user</summary>
/// <remarks>
/// Author: Amulya Khare
/// </remarks>
namespace ACPAddIn
{
    partial class Tour
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
            this.TourScreen1 = new System.Windows.Forms.PictureBox();
            this.TourScreen2 = new System.Windows.Forms.PictureBox();
            this.next_btn = new System.Windows.Forms.PictureBox();
            this.back_btn = new System.Windows.Forms.PictureBox();
            this.tourScreen0 = new System.Windows.Forms.PictureBox();
            this.tourScreen3 = new System.Windows.Forms.PictureBox();
            this.tourScreen4 = new System.Windows.Forms.PictureBox();
            this.finish_btn = new System.Windows.Forms.Button();
            this.checkBox1 = new System.Windows.Forms.CheckBox();
            this.extend_word_lbl = new System.Windows.Forms.Label();
            this.reduce_word_lbl = new System.Windows.Forms.Label();
            this.extend_sentence_lbl = new System.Windows.Forms.Label();
            this.reduce_sentence_lbl = new System.Windows.Forms.Label();
            this.extend_para_lbl = new System.Windows.Forms.Label();
            this.reduce_para_lbl = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.TourScreen1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.TourScreen2)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.next_btn)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.back_btn)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.tourScreen0)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.tourScreen3)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.tourScreen4)).BeginInit();
            this.SuspendLayout();
            // 
            // TourScreen1
            // 
            this.TourScreen1.Image = global::ACPAddIn.Properties.Resources.ACPScreen1;
            this.TourScreen1.Location = new System.Drawing.Point(-3, -1);
            this.TourScreen1.Name = "TourScreen1";
            this.TourScreen1.Size = new System.Drawing.Size(604, 368);
            this.TourScreen1.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.TourScreen1.TabIndex = 0;
            this.TourScreen1.TabStop = false;
            // 
            // TourScreen2
            // 
            this.TourScreen2.Image = global::ACPAddIn.Properties.Resources.ACPScreen2;
            this.TourScreen2.Location = new System.Drawing.Point(-3, -1);
            this.TourScreen2.Name = "TourScreen2";
            this.TourScreen2.Size = new System.Drawing.Size(604, 368);
            this.TourScreen2.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.TourScreen2.TabIndex = 2;
            this.TourScreen2.TabStop = false;
            // 
            // next_btn
            // 
            this.next_btn.BackgroundImageLayout = System.Windows.Forms.ImageLayout.None;
            this.next_btn.Image = global::ACPAddIn.Properties.Resources.next_icon;
            this.next_btn.Location = new System.Drawing.Point(519, 314);
            this.next_btn.Name = "next_btn";
            this.next_btn.Size = new System.Drawing.Size(44, 43);
            this.next_btn.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.next_btn.TabIndex = 3;
            this.next_btn.TabStop = false;
            this.next_btn.Click += new System.EventHandler(this.next_btn_Click);
            // 
            // back_btn
            // 
            this.back_btn.BackgroundImageLayout = System.Windows.Forms.ImageLayout.None;
            this.back_btn.Image = global::ACPAddIn.Properties.Resources.back_icon;
            this.back_btn.Location = new System.Drawing.Point(32, 314);
            this.back_btn.Name = "back_btn";
            this.back_btn.Size = new System.Drawing.Size(44, 43);
            this.back_btn.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.back_btn.TabIndex = 4;
            this.back_btn.TabStop = false;
            this.back_btn.Click += new System.EventHandler(this.back_btn_Click);
            // 
            // tourScreen0
            // 
            this.tourScreen0.Image = global::ACPAddIn.Properties.Resources.ACPScreen0;
            this.tourScreen0.Location = new System.Drawing.Point(-3, 0);
            this.tourScreen0.Name = "tourScreen0";
            this.tourScreen0.Size = new System.Drawing.Size(604, 368);
            this.tourScreen0.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.tourScreen0.TabIndex = 5;
            this.tourScreen0.TabStop = false;
            // 
            // tourScreen3
            // 
            this.tourScreen3.Image = global::ACPAddIn.Properties.Resources.ACPScreen3;
            this.tourScreen3.Location = new System.Drawing.Point(-3, -1);
            this.tourScreen3.Name = "tourScreen3";
            this.tourScreen3.Size = new System.Drawing.Size(604, 368);
            this.tourScreen3.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.tourScreen3.TabIndex = 6;
            this.tourScreen3.TabStop = false;
            // 
            // tourScreen4
            // 
            this.tourScreen4.Image = global::ACPAddIn.Properties.Resources.ACPScreen4;
            this.tourScreen4.Location = new System.Drawing.Point(-3, -1);
            this.tourScreen4.Name = "tourScreen4";
            this.tourScreen4.Size = new System.Drawing.Size(604, 368);
            this.tourScreen4.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.tourScreen4.TabIndex = 7;
            this.tourScreen4.TabStop = false;
            // 
            // finish_btn
            // 
            this.finish_btn.Location = new System.Drawing.Point(503, 325);
            this.finish_btn.Name = "finish_btn";
            this.finish_btn.Size = new System.Drawing.Size(75, 23);
            this.finish_btn.TabIndex = 8;
            this.finish_btn.Text = "Finish";
            this.finish_btn.UseVisualStyleBackColor = true;
            this.finish_btn.Click += new System.EventHandler(this.finish_btn_Click);
            // 
            // checkBox1
            // 
            this.checkBox1.AutoSize = true;
            this.checkBox1.Location = new System.Drawing.Point(389, 329);
            this.checkBox1.Name = "checkBox1";
            this.checkBox1.Size = new System.Drawing.Size(108, 17);
            this.checkBox1.TabIndex = 9;
            this.checkBox1.Text = "Don\'t show again";
            this.checkBox1.UseVisualStyleBackColor = true;
            // 
            // extend_word_lbl
            // 
            this.extend_word_lbl.AutoSize = true;
            this.extend_word_lbl.Location = new System.Drawing.Point(285, 197);
            this.extend_word_lbl.Name = "extend_word_lbl";
            this.extend_word_lbl.Size = new System.Drawing.Size(0, 13);
            this.extend_word_lbl.TabIndex = 10;
            this.extend_word_lbl.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // reduce_word_lbl
            // 
            this.reduce_word_lbl.AutoSize = true;
            this.reduce_word_lbl.Location = new System.Drawing.Point(396, 197);
            this.reduce_word_lbl.Name = "reduce_word_lbl";
            this.reduce_word_lbl.Size = new System.Drawing.Size(0, 13);
            this.reduce_word_lbl.TabIndex = 11;
            this.reduce_word_lbl.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // extend_sentence_lbl
            // 
            this.extend_sentence_lbl.AutoSize = true;
            this.extend_sentence_lbl.Location = new System.Drawing.Point(285, 219);
            this.extend_sentence_lbl.Name = "extend_sentence_lbl";
            this.extend_sentence_lbl.Size = new System.Drawing.Size(0, 13);
            this.extend_sentence_lbl.TabIndex = 12;
            this.extend_sentence_lbl.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // reduce_sentence_lbl
            // 
            this.reduce_sentence_lbl.AutoSize = true;
            this.reduce_sentence_lbl.Location = new System.Drawing.Point(396, 219);
            this.reduce_sentence_lbl.Name = "reduce_sentence_lbl";
            this.reduce_sentence_lbl.Size = new System.Drawing.Size(0, 13);
            this.reduce_sentence_lbl.TabIndex = 13;
            this.reduce_sentence_lbl.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // extend_para_lbl
            // 
            this.extend_para_lbl.AutoSize = true;
            this.extend_para_lbl.Location = new System.Drawing.Point(285, 241);
            this.extend_para_lbl.Name = "extend_para_lbl";
            this.extend_para_lbl.Size = new System.Drawing.Size(0, 13);
            this.extend_para_lbl.TabIndex = 14;
            this.extend_para_lbl.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // reduce_para_lbl
            // 
            this.reduce_para_lbl.AutoSize = true;
            this.reduce_para_lbl.Location = new System.Drawing.Point(396, 241);
            this.reduce_para_lbl.Name = "reduce_para_lbl";
            this.reduce_para_lbl.Size = new System.Drawing.Size(0, 13);
            this.reduce_para_lbl.TabIndex = 15;
            this.reduce_para_lbl.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // Tour
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.White;
            this.ClientSize = new System.Drawing.Size(599, 369);
            this.Controls.Add(this.reduce_para_lbl);
            this.Controls.Add(this.extend_para_lbl);
            this.Controls.Add(this.reduce_sentence_lbl);
            this.Controls.Add(this.extend_sentence_lbl);
            this.Controls.Add(this.reduce_word_lbl);
            this.Controls.Add(this.extend_word_lbl);
            this.Controls.Add(this.checkBox1);
            this.Controls.Add(this.finish_btn);
            this.Controls.Add(this.back_btn);
            this.Controls.Add(this.next_btn);
            this.Controls.Add(this.tourScreen3);
            this.Controls.Add(this.tourScreen4);
            this.Controls.Add(this.TourScreen2);
            this.Controls.Add(this.TourScreen1);
            this.Controls.Add(this.tourScreen0);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "Tour";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "AutoComPaste Tour";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.Tour_FormClosed);
            ((System.ComponentModel.ISupportInitialize)(this.TourScreen1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.TourScreen2)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.next_btn)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.back_btn)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.tourScreen0)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.tourScreen3)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.tourScreen4)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox TourScreen1;
        private System.Windows.Forms.PictureBox TourScreen2;
        private System.Windows.Forms.PictureBox next_btn;
        private System.Windows.Forms.PictureBox back_btn;
        private System.Windows.Forms.PictureBox tourScreen0;
        private System.Windows.Forms.PictureBox tourScreen3;
        private System.Windows.Forms.PictureBox tourScreen4;
        private System.Windows.Forms.Button finish_btn;
        private System.Windows.Forms.CheckBox checkBox1;
        private System.Windows.Forms.Label extend_word_lbl;
        private System.Windows.Forms.Label reduce_word_lbl;
        private System.Windows.Forms.Label extend_sentence_lbl;
        private System.Windows.Forms.Label reduce_sentence_lbl;
        private System.Windows.Forms.Label extend_para_lbl;
        private System.Windows.Forms.Label reduce_para_lbl;
    }
}