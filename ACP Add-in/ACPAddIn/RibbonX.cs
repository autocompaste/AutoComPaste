using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using Office = Microsoft.Office.Core;
using System.Net.Sockets;
using System.Windows.Forms;

namespace ACPAddIn
{
    /// <summary>
    /// This class is the presentation layer which display customize ribbon control for ACP.
    /// 
    /// Author: Loke Yan Hao, Amulya
    /// </summary>
    [ComVisible(true)]
    public class RibbonX : Office.IRibbonExtensibility
    {
        private Office.IRibbonUI ribbonUI;

        public string GetCustomUI(string ribbonID)
        {
            return Properties.Resources.RibbonX;
        }

        public void OnLoad(Office.IRibbonUI r)
        {
            ribbonUI = r;
        }

        public void OnAutomaticModeButtonPressed(Office.IRibbonControl control, bool isPressed)
        {
            Globals.ThisAddIn.Mode = ThisAddIn.TriggerMode.AUTO_TRIGGER;
            ribbonUI.Invalidate();
        }

        public void OnManualModeButtonPressed(Office.IRibbonControl control, bool isPressed)
        {
            Globals.ThisAddIn.Mode = ThisAddIn.TriggerMode.MANUAL_TRIGGER;
            ribbonUI.Invalidate();
        }

        public void OnHelpClick(Office.IRibbonControl control)
        {
            try
            {
                ThisAddIn.tourForm.ShowDialog();
            }
            catch (Exception e)
            {
                MessageBox.Show("AutoComPaste is currently disabled. Please run the AutoComPaste application and restart this document.", "AutoComPaste Help", MessageBoxButtons.OK, MessageBoxIcon.Error, MessageBoxDefaultButton.Button1);
            }
        }

        public bool OnGetPressed(Office.IRibbonControl control)
        {
            bool isPressed = false; 
            switch (control.Id)
            {
                case "autoModeButton":
                    isPressed = (Globals.ThisAddIn.Mode == ThisAddIn.TriggerMode.AUTO_TRIGGER);
                    break;
                case "manualModeButton":
                    isPressed = (Globals.ThisAddIn.Mode == ThisAddIn.TriggerMode.MANUAL_TRIGGER);
                    break;
            }
            return isPressed;
        }

        public void OnClearHookButton(Office.IRibbonControl control)
        {

        }
    }
}
