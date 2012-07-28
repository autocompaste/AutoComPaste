 using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using Microsoft.Office.Tools.Word;
using Microsoft.Office.Tools.Word.Extensions;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.Windows.Forms;
using System.Drawing;
using System.Threading;
using ACPAddIn.Object_Class;
using System.Timers;
using System.Windows.Threading;
using System.IO;
using IniParser;
using System.ComponentModel;
using System.Net.Sockets;

namespace ACPAddIn
{
    /// <summary>
    /// This is the main entry point for the Add-in. It contain methods to initialize and clean up the program.
    /// It also attach to keyboard and mouse hook, and process their events.
    /// 
    /// Author: Loke Yan Hao, Teo Kee Cheng, Ng Chin Hui
    /// </summary>
    public partial class ThisAddIn
    {
        public enum TriggerMode
        {
            AUTO_TRIGGER, MANUAL_TRIGGER
        };

        // Configuration variables
        String configurationFilePath;
        private int _entriesInView;
        public int EntriesInView {
            get
            {
                if (_entriesInView <= 0)
                {
                    return 1;
                }
                
                return _entriesInView;
            } 
            set {
                if (value <= 0)
                {
                    this._entriesInView = 1;
                }
                else if(value >= 10)
                {
                    this._entriesInView = 9;
                }
                else{
                    this._entriesInView = value;
                }
            } 
        }
        private TriggerMode _mode;
        public TriggerMode Mode { 
            get { return _mode; } 
            set { this._mode = value; } 
        }
        private int _triggerDelay;
        public int TriggerDelay
        {
            get { return _triggerDelay; }
            set { this._triggerDelay = value; }
        }

        private int mouseHookHandle = 0;
        private int keyboardHookHandle = 0;
        private HookProc keyboardHookProcedure;
        private HookProc mouseHookProcedure;
        private const int WH_KEYBOARD_LL = 13;
        private const int WH_MOUSE = 7;
        private const int WH_MOUSE_LL = 14;
        private NativeWindow wordMain;
        System.Timers.Timer checkServerStatusTimer;

        private Point applicationLocation;
        private Size applicationSize;
        private float dpi;
        private IniData config;
        public IniData Configuration
        {
            get { return config; }
        }

        private AutoCompleteForm autoCompleteForm;
        private Notification notificationForm;
        public static Tour tourForm;
        private ExtendSuggestionHint extendSuggestionForm;
        private bool tourEnabled;
        
        private int handle;
        private Word.Selection currentSelection;

        private Client logic;

        private Dispatcher _dispatcher;
        public Dispatcher Dispatcher { 
            get { return _dispatcher; } 
        }

        FileSystemWatcher watchConfigFile;
        bool onConfigChange = false;

        private System.Timers.Timer triggerSuggTimer = null;

        private bool isEnabled = true;
        private bool isDisablePopUp = false;

        // for User Testing
        private Boolean isUserActionLogging = false;
        private String logContent = "";
        private String startTestTime = "";
        private String endTestTime = "";
        private DateTime startSwitchTime;
        private DateTime endSwitchTime;
        private DateTime startReqSuggTime;
        private DateTime endReqSuggTime;

        private ExtensionMode extMode = null;
        private BackgroundWorker extensionBw = null;

        private string ERROR_CONNECTION = "AutoComPaste is not running.";
        private string CONNECTION_UP = "AutoComPaste is running.";

        #region Variables for hotkey
        // hotkeys for other functions
        public int triggerSuggestionPopUpKey;
        public String triggerSuggestionPopUpKeyMod1;
        public String triggerSuggestionPopUpKeyMod2;

        // hotkeys for extend suggestions
        private int extendWordKey;
        private String extendWordKeyMod1;
        private String extendWordKeyMod2;
        private int reduceWordKey;
        private String reduceWordKeyMod1;
        private String reduceWordKeyMod2;
        private int extendSentenceKey;
        private String extendSentenceKeyMod1;
        private String extendSentenceKeyMod2;
        private int reduceSentenceKey;
        private String reduceSentenceKeyMod1;
        private String reduceSentenceKeyMod2;
        private int extendParagraphKey;
        private String extendParagraphKeyMod1;
        private String extendParagraphKeyMod2;
        private int reduceParagraphKey;
        private String reduceParagraphKeyMod1;
        private String reduceParagraphKeyMod2;
        #endregion

        #region DllImports

        // Hook procedure callback type.
        private delegate int HookProc(
            int nCode, IntPtr wParam, IntPtr lParam);

        // Managed equivalent of the POINT struct defined in winuser.h.
        [StructLayout(LayoutKind.Sequential)]
        private struct POINT
        {
            public int x;
            public int y;
        }

        // Managed equivalent of the KBDLLHOOKSTRUCT defined in winuser.h.
        [StructLayout(LayoutKind.Sequential)]
        private struct KbDllHookStruct
        {
            internal int vkCode;
            internal int scanCode;
            internal int flags;
            internal int time;
            internal int dwExtraInfo;
        }

        // Managed equivalent of the MOUSEHOOKSTRUCT defined in winuser.h.
        [StructLayout(LayoutKind.Sequential)]
        private class MouseHookStruct
        {
            internal POINT pt;
            internal int hwnd;
            internal int wHitTestCode;
            internal int dwExtraInfo;
        }


        // SetWindowsHookEx is used to install a thread-specific hook.
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        private static extern int SetWindowsHookEx(
            int idHook, HookProc lpfn, IntPtr hInstance, int threadId);

        // UnhookWindowsHookEx is used to uninstall the hook.
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        private static extern bool UnhookWindowsHookEx(int idHook);

        // CallNextHookEx is used to pass the hook information to the next
        // hook procedure in the chain.
        [DllImport("user32.dll", CharSet = CharSet.Auto,
         CallingConvention = CallingConvention.StdCall)]
        private static extern int CallNextHookEx(
            int idHook, int nCode, IntPtr wParam, IntPtr lParam);

        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        private static extern ushort GetAsyncKeyState(int vKey);

        // GetModuleHandle is used when calling SetWindowsHookEx for
        // a keyboard hook.
        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        private static extern IntPtr GetModuleHandle(string lpModuleName);

        [DllImport("user32.dll", SetLastError = true)]
        public static extern IntPtr SetActiveWindow(IntPtr hWnd);

        #endregion

        # region DllImportsForCaret

        [StructLayout(LayoutKind.Sequential)]    // Required by user32.dll
        public struct RECT
        {
            public uint Left;
            public uint Top;
            public uint Right;
            public uint Bottom;
        };

        [StructLayout(LayoutKind.Sequential)]    // Required by user32.dll
        public struct GUITHREADINFO
        {
            public uint cbSize;
            public uint flags;
            public IntPtr hwndActive;
            public IntPtr hwndFocus;
            public IntPtr hwndCapture;
            public IntPtr hwndMenuOwner;
            public IntPtr hwndMoveSize;
            public IntPtr hwndCaret;
            public RECT rcCaret;
        };

        /*- Retrieves Title Information of the specified window -*/
        [DllImport("user32.dll")]
        static extern int GetWindowText(int hWnd, StringBuilder text, int count);

        /*- Retrieves information about active window or any specific GUI thread -*/
        [DllImport("user32.dll", EntryPoint = "GetGUIThreadInfo")]
        public static extern bool GetGUIThreadInfo(uint tId, out GUITHREADINFO threadInfo);

        /*- Retrieves Handle to the ForeGroundWindow -*/
        [DllImport("user32.dll")]
        public static extern IntPtr GetForegroundWindow();

        [DllImport("user32.dll")]
        public static extern IntPtr GetActiveWindow();

        /*- Converts window specific point to screen specific -*/
        [DllImport("user32.dll")]
        public static extern bool ClientToScreen(IntPtr hWnd, out Point position);

        GUITHREADINFO guiInfo;                     // To store GUI Thread Information

        # endregion  

        #region Setup/Teardown Add In
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);

            this.Application.DocumentBeforeClose += new Word.ApplicationEvents4_DocumentBeforeCloseEventHandler(delegate(Word.Document doc, ref bool Cancel)
            {
                handle = (int)GetActiveWindow();

                if (isEnabled)
                {
                    BackgroundWorker b = new BackgroundWorker();
                    b.DoWork += delegate(object sender, DoWorkEventArgs e)
                    {
                        try
                        {
                            // Inform ACP to close the Document           
                            logic.closeDestinationDocument(handle);
                        }
                        catch (SocketException e2)
                        {
                        }

                        // Logging for user testing
                        if (isUserActionLogging)
                        {
                            endTestTime = "END TIME," + DateTime.Now.ToString("HH:mm:ss") + "\r";
                            writeToFile(logContent);
                        }
                    };
                    b.RunWorkerAsync();
                }
                else
                {
                    if (checkServerStatusTimer != null)
                    {
                        checkServerStatusTimer.Stop();
                    }
                }

            });
        }
        
        protected override Microsoft.Office.Core.IRibbonExtensibility CreateRibbonExtensibilityObject()
        {
            return new RibbonX();
        }

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            _dispatcher = Dispatcher.CurrentDispatcher; 

            SetKeyboardHook();
            SetMouseHook();

            // Track the handle ID for the text editor (Microsoft Word)
            int handle = (int) GetActiveWindow();
            currentSelection = Application.Selection;

            // Initialize forms and set Microsoft Word as the parent window
            autoCompleteForm = new AutoCompleteForm();
            notificationForm = new Notification();
            extendSuggestionForm = new ExtendSuggestionHint();

            Application.WindowActivate += new Word.ApplicationEvents4_WindowActivateEventHandler(Application_WindowActivate);
            Application.WindowDeactivate += new Word.ApplicationEvents4_WindowDeactivateEventHandler(Application_WindowDeactivate);
            Application.WindowSize += new Microsoft.Office.Interop.Word.ApplicationEvents4_WindowSizeEventHandler(OnWindowSizeChange);

            //Convert Words DPI based Height and Width
            System.Drawing.Graphics g = notificationForm.CreateGraphics();
            dpi = g.DpiX;
            
            updateApplicationSizeDetail();

            logic = new Client();

            bool serverStatus = initConfiguration();
            extMode = new ExtensionMode(logic);

            if (serverStatus)
            {
                tourForm = new Tour(this.config, configurationFilePath);
                if (tourEnabled)
                {
                    tourForm.ShowDialog();
                }
            }
            else
            {
                // Setting this to true allow ACP to inform user that ACP is not been running
                isDisablePopUp = true;
            }

            // for user testing
            startTestTime = "START TIME," + DateTime.Now.ToString("HH:mm:ss") + "\r";
        }

        private void Application_WindowDeactivate(Word.Document Doc, Word.Window Wn)
        {
            // for User Testing
            startSwitchTime = DateTime.Now;

            // The following 3 lines are used to ensure that the Microsoft Word main 
            // window is set as the main focus, instead of the child forms/window.
            autoCompleteForm.Hide();
            notificationForm.Hide();
            extendSuggestionForm.Hide();
        }

        private void Application_WindowActivate(Word.Document Doc, Word.Window Wn)
        {
            // for User Testing
            endSwitchTime = DateTime.Now;
            appendSwitchWindowString();

            // Track the handle ID for the text editor (Microsoft Word)
            int tempHandle = (int)GetActiveWindow();
            if (handle != tempHandle)
            {
                handle = (int)GetActiveWindow();
                currentSelection = Application.Selection;

                // Retrieve handle of the Microsoft Word window.
                wordMain = new NativeWindow();
                wordMain.AssignHandle(new IntPtr(handle));

                // Hide the form off the screen
                autoCompleteForm.Location = new Point(-1000, -1000);
                notificationForm.Location = new Point(-1000, -1000);
                extendSuggestionForm.Location = new Point(-1000, -1000);

                // Reassign forms' parent
                autoCompleteForm.Visible = false;
                autoCompleteForm.showForm(wordMain); // Assign the form parent to the current active window
                autoCompleteForm.Focus(); // In order to make the child form appear infront of the parent form
                autoCompleteForm.Hide();

                notificationForm.Visible = false;
                notificationForm.Show(wordMain); // Assign the form parent to the current active window
                notificationForm.Focus(); // In order to make the child form appear infront of the parent form
                notificationForm.Hide();

                extendSuggestionForm.Visible = false;
                extendSuggestionForm.Show(wordMain);
                extendSuggestionForm.Focus();
                extendSuggestionForm.Hide();

                wordMain.ReleaseHandle();
            }

            if (!isEnabled && isDisablePopUp)
            {
                Globals.ThisAddIn.Dispatcher.Invoke(new displayMessageDelegate(displayMessage), new Object[] { ERROR_CONNECTION });
                
                // Disable the pop-up
                isDisablePopUp = false;
            }

            BackgroundWorker b = new BackgroundWorker();
            b.DoWork += delegate(object sender2, DoWorkEventArgs e2)
            {
                try
                {
                    if (isEnabled)
                    {
                        // Inform ACP to change destination document
                        logic.setDestinationDocument(handle);
                    }
                }
                catch (SocketException e)
                {
                    Globals.ThisAddIn.Dispatcher.Invoke(new displayMessageDelegate(displayMessage), new Object[] { ERROR_CONNECTION });
                    markServerDown();
                }
            };
            b.RunWorkerAsync();

            updateApplicationSizeDetail();
            SetActiveWindow(new IntPtr(tempHandle));
        }

        private void OnWindowSizeChange(Microsoft.Office.Interop.Word.Document Doc, Microsoft.Office.Interop.Word.Window Wn)
        {
            updateApplicationSizeDetail();
        }

        private void updateApplicationSizeDetail()
        {
            int height = (int)(Application.Height * dpi / 72);
            int width = (int)(Application.Width * dpi / 72);
            int top = (int)(Application.Top * dpi / 72);
            int left = (int)(Application.Left * dpi / 72);

            applicationSize = new Size(width, height);
            applicationLocation = new Point(left, top);

            notificationForm.updateLocation(applicationLocation, applicationSize);
            extendSuggestionForm.updateLocation(applicationLocation, applicationSize);
        }

        // x and y is the coordinate for the mouse cursor
        private bool isCoordinateWithinForm(int x, int y, Form form)
        {
            Point p = autoCompleteForm.Location;
            Size s = autoCompleteForm.Size;

            if (x > p.X && x < p.X + s.Width
                && y > p.Y && y < p.Y + s.Height)
            {
                return true;
            }

            return false;
        }

        private void ClearHook()
        {
            if (mouseHookHandle != 0 && keyboardHookHandle != 0)
            {
                bool ret = UnhookWindowsHookEx(mouseHookHandle);
                bool ret2 = UnhookWindowsHookEx(keyboardHookHandle);

                if (ret == false && ret2 == false)
                {
                    Debug.WriteLine("UnhookWindowsHookEx Failed");
                    return;
                }
                mouseHookHandle = 0;
                keyboardHookHandle = 0;
            }
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            ClearHook();
            autoCompleteForm.Dispose();
        }

        private void markServerDown()
        {
            if (isEnabled)
            {
                isEnabled = false;

                // Create a timer to poll server status
                checkServerStatusTimer = new System.Timers.Timer();
                checkServerStatusTimer.Elapsed += new ElapsedEventHandler(delegate(Object sender, ElapsedEventArgs e)
                {
                    BackgroundWorker statusPollingWorker = new BackgroundWorker();
                    statusPollingWorker.DoWork += delegate(object sender2, DoWorkEventArgs e2)
                    {
                        if (logic.checkServerAlive() && 
                            initConfiguration())
                        {
                            checkServerStatusTimer.Stop();
                            Globals.ThisAddIn.Dispatcher.Invoke(new displayMessageDelegate(displayMessage), new Object[] { CONNECTION_UP });
                            isEnabled = true;
                        }
                    };
                    statusPollingWorker.RunWorkerAsync();
                });

                checkServerStatusTimer.Interval = 3000;
                checkServerStatusTimer.Start();
            }

        }

        #endregion

        #region MouseHook
        private void SetMouseHook()
        {
            ClearHook();

            // Create an instance of the HookProc delegate.
            mouseHookProcedure = new HookProc(MouseHookProc);
            mouseHookHandle =
                SetWindowsHookEx(WH_MOUSE, mouseHookProcedure,
                    (IntPtr)0, AppDomain.GetCurrentThreadId());
        }

        private int MouseHookProc(int nCode, IntPtr wParam, IntPtr lParam)
        {
            // Per docs, if nCode < 0, the hook procedure must pass the 
            // message to CallNextHookEx function without further processing
            // and should return the value returned by CallNextHookEx. 
            if (nCode < 0)
            {
                return CallNextHookEx(mouseHookHandle, nCode, wParam, lParam);
            }
            else
            {
                // The lparam that Windows passes us is a pointer to a 
                // MouseHookStruct.
                MouseHookStruct mouseHookStruct =
                    (MouseHookStruct)
                    Marshal.PtrToStructure(lParam, typeof(MouseHookStruct));

                // Hide the auto-completion box when a mouse click is detected outside the box.
                if (!isCoordinateWithinForm(mouseHookStruct.pt.x, mouseHookStruct.pt.y, autoCompleteForm))
                {
                    if (autoCompleteForm.Visible && ((WM)wParam.ToInt32() == WM.LBUTTONDOWN || (WM)wParam.ToInt32() == WM.NCLBUTTONDOWN))
                    {
                        autoCompleteForm.Hide();
                    }
                }

                // Hide the auto-completion box when a mouse wheel event is detected.
                if (autoCompleteForm.Visible && (WM)wParam.ToInt32() == WM.MOUSEWHEEL)
                {
                    autoCompleteForm.Hide();
                }

                if (autoCompleteForm.Visible && (WM)wParam.ToInt32() == WM.MOUSEHWHEEL)
                {
                    autoCompleteForm.Hide();
                }

                if (((WM)wParam.ToInt32() == WM.LBUTTONDOWN || (WM)wParam.ToInt32() == WM.NCLBUTTONDOWN))
                {
                    if (extMode.isExtensionMode())
                    {
                        extMode.resetExtensionMode();
                        extendSuggestionForm.fadeOut();
                    }
                }

                // Ensure that we don't break the hook chain.
                return CallNextHookEx(mouseHookHandle, nCode, wParam, lParam);
            }
        }
        #endregion

        #region KeyboardHook
        private void SetKeyboardHook()
        {
            ClearHook();

            // Create an instance of the HookProc delegate.
            keyboardHookProcedure = new HookProc(KeyboardHookProc);

            using (Process curProcess = Process.GetCurrentProcess())
            using (ProcessModule curModule = curProcess.MainModule)
            {
                keyboardHookHandle =
                    SetWindowsHookEx(WH_KEYBOARD_LL, keyboardHookProcedure,
                    GetModuleHandle(curModule.ModuleName), 0);
            }
        }

        private int KeyboardHookProc(int nCode, IntPtr wParam, IntPtr lParam)
        {
            int tempHandle = (int)GetActiveWindow();

            // The lparam that Windows passes us is a pointer to a 
            // KbDllHookStruct.
            KbDllHookStruct kbDllHookStruct = (KbDllHookStruct)Marshal.PtrToStructure(lParam, typeof(KbDllHookStruct));

            if (autoCompleteForm.Visible)
            {
                // Log only UI shortcut keys
                if ((wParam == (IntPtr)WM.KEYDOWN)
                    || (wParam == (IntPtr)WM.SYSKEYDOWN))
                {
                    // Logging for user testing
                    appendLogString(kbDllHookStruct.vkCode);
                }
            }

            // Response to keystrokes only if the active window is the text-editor (Microsoft Word)
            if (handle == tempHandle && isEnabled)
            {
                if ((wParam == (IntPtr)WM.KEYDOWN)
                    || (wParam == (IntPtr)WM.SYSKEYDOWN))
                {                   
                    if (extMode.isExtensionMode())
                    {
                        // Logging for user testing
                        appendLogString(kbDllHookStruct.vkCode);

                        bool result = handleExtensionMode(kbDllHookStruct);
                        bool isBlockKey = !result;

                        if (isBlockKey)
                        {
                            extendSuggestionForm.fadeOut();
                            return -1;
                        }
                    }
                    else
                    {
                        bool result = handleRequestSuggestionMode(kbDllHookStruct);
                        bool isBlockKey = !result;

                        if (isBlockKey)
                        {
                            return -1;
                        }
                    }
                }
            }

            return CallNextHookEx(keyboardHookHandle, nCode, wParam, lParam);
        }
        #endregion 

        #region Config File
        private bool initConfiguration()
        {
            configurationFilePath = "";
            try
            {
                configurationFilePath = logic.getFilePath();
                trackFile(configurationFilePath);
                readConfiguration(configurationFilePath);
            }
            catch (System.Net.Sockets.SocketException error)
            {
                markServerDown();

                return false;
            }

            return true;
        }

        private void trackFile(String filePath)
        {
            int index = filePath.LastIndexOf("\\");
            String fileName = filePath.Substring(index + 1);
            String fileDirectory = filePath.Substring(0, index);

            // Track configuration file changes
            watchConfigFile = new FileSystemWatcher();
            watchConfigFile.Path = fileDirectory;
            watchConfigFile.Filter = fileName;
            watchConfigFile.Changed += new FileSystemEventHandler(OnConfigChange);
            watchConfigFile.EnableRaisingEvents = true;
            watchConfigFile.NotifyFilter = NotifyFilters.LastWrite;
        }

        private void readConfiguration(String filePath)
        {
            FileIniDataParser parser = new FileIniDataParser();

            int i = 3;

            while (i != 0)
            {
                try
                {
                    config = parser.LoadFile(filePath);

                    EntriesInView = Convert.ToInt32(config["settings"]["entriesInView"]);
                    Boolean autoTrigger = Convert.ToBoolean(config["settings"]["autoTrigger"]);
                    isEnabled = Convert.ToBoolean(config["settings"]["enableDisable"]);
                    TriggerDelay = Convert.ToInt32(config["settings"]["triggerDelay"]);
                    this.tourEnabled = Convert.ToBoolean(config["settings"]["tourEnabled"]);
                    setHotkeys(config);

                    if (autoTrigger)
                    {
                        Mode = TriggerMode.AUTO_TRIGGER;
                    }
                    else
                    {
                        Mode = TriggerMode.MANUAL_TRIGGER;
                    }
                    break;
                }
                catch (ParsingException e)
                {
                    Thread.Sleep(100);
                    i--;
                }
            }
        }

        // To allow background worker thread to display the suggestion box on the UI thread
        private delegate void ReflectConfigurationChange(String filePath);
        private void reflectConfigurationChange(String filePath)
        {
            autoCompleteForm.Hide();
            readConfiguration(filePath);
        }

        private void OnConfigChange(object sender, FileSystemEventArgs e)
        {
            // There is a bug with the framework: 
            // When there is a file changed event detected, this method will be triggered twice
            // The following conditional check helps to prevent this bug from triggering our code twice
            if (!onConfigChange)
            {
                onConfigChange = true;
                Globals.ThisAddIn.Dispatcher.Invoke(new ReflectConfigurationChange(reflectConfigurationChange), e.FullPath);
            }
            else
            {
                onConfigChange = false;
            }
        }
        #endregion

        #region HotKey
        private void setHotkeys(IniData config)
        {
            triggerSuggestionPopUpKeyMod1 = "0";
            triggerSuggestionPopUpKeyMod2 = "0";

            extendWordKeyMod1 = "0";
            extendWordKeyMod2 = "0";
            reduceWordKeyMod1 = "0";
            reduceWordKeyMod2 = "0";
            extendSentenceKeyMod1 = "0";
            extendSentenceKeyMod2 = "0";
            reduceSentenceKeyMod1 = "0";
            reduceSentenceKeyMod2 = "0";
            extendParagraphKeyMod1 = "0";
            extendParagraphKeyMod2 = "0";
            reduceParagraphKeyMod1 = "0";
            reduceParagraphKeyMod2 = "0";

            String str;
            String[] strArr;

            /* Retrieving and Setting Hotkeys for other functions */
            str = config["hotkeys"]["triggerSuggestionPopUp"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    triggerSuggestionPopUpKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    triggerSuggestionPopUpKeyMod1 = strArr[i].Trim(' ');
                else
                    triggerSuggestionPopUpKeyMod2 = strArr[i].Trim(' ');
            }

            /* Retrieving and Setting Hotkeys for extend Suggestion */
            str = config["hotkeys"]["extendWord"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    extendWordKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    extendWordKeyMod1 = strArr[i].Trim(' ');
                else
                    extendWordKeyMod2 = strArr[i].Trim(' ');
            }

            str = config["hotkeys"]["reduceWord"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    reduceWordKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    reduceWordKeyMod1 = strArr[i].Trim(' ');
                else
                    reduceWordKeyMod2 = strArr[i].Trim(' ');
            }

            str = config["hotkeys"]["extendSentence"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    extendSentenceKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    extendSentenceKeyMod1 = strArr[i].Trim(' ');
                else
                    extendSentenceKeyMod2 = strArr[i].Trim(' ');
            }

            str = config["hotkeys"]["reduceSentence"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    reduceSentenceKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    reduceSentenceKeyMod1 = strArr[i].Trim(' ');
                else
                    reduceSentenceKeyMod2 = strArr[i].Trim(' ');
            }

            str = config["hotkeys"]["extendParagraph"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    extendParagraphKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    extendParagraphKeyMod1 = strArr[i].Trim(' ');
                else
                    extendParagraphKeyMod2 = strArr[i].Trim(' ');
            }

            str = config["hotkeys"]["reduceParagraph"];
            strArr = str.Split('+');
            for (int i = strArr.Length - 1; i >= 0; i--)
            {
                if (i == strArr.Length - 1)
                    reduceParagraphKey = KeyboardMapping.Keys[strArr[i].Trim(' ')];
                else if (i == strArr.Length - 2)
                    reduceParagraphKeyMod1 = strArr[i].Trim(' ');
                else
                    reduceParagraphKeyMod2 = strArr[i].Trim(' ');
            }
        }

        private Boolean checkHotkeysModifiers(String function)
        {
            Boolean result = false;
            Boolean result1 = false;
            Boolean result2 = false;
            int VK_LCONTROL = 0xA2;
            int VK_RCONTROL = 0xA3;
            int VK_LALT = 0xA4;
            int VK_RALT = 0xA5;
            int VK_LSHIFT = 0xA0;
            int VK_RSHIFT = 0xA1;
            
            switch (function)
            {
                case "triggerSuggestionPopUp":
                    if (triggerSuggestionPopUpKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (triggerSuggestionPopUpKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (triggerSuggestionPopUpKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (triggerSuggestionPopUpKeyMod1.Equals("0"))
                        result1 = true;

                    if (triggerSuggestionPopUpKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (triggerSuggestionPopUpKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (triggerSuggestionPopUpKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (triggerSuggestionPopUpKeyMod2.Equals("0"))
                        result2 = true;
                    break;

                case "extendWord":
                    if (extendWordKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (extendWordKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (extendWordKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (extendWordKeyMod1.Equals("0"))
                        result1 = true;

                    if (extendWordKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (extendWordKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (extendWordKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (extendWordKeyMod2.Equals("0"))
                        result2 = true;
                    break;

                case "removeWord":
                    if (reduceWordKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (reduceWordKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (reduceWordKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (reduceWordKeyMod1.Equals("0"))
                        result1 = true;

                    if (reduceWordKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (reduceWordKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (reduceWordKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (reduceWordKeyMod2.Equals("0"))
                        result2 = true;
                    break;

                case "extendSentence":
                    if (extendSentenceKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (extendSentenceKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (extendSentenceKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (extendSentenceKeyMod1.Equals("0"))
                        result1 = true;

                    if (extendSentenceKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (extendSentenceKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (extendSentenceKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (extendSentenceKeyMod2.Equals("0"))
                        result2 = true;
                    break;

                case "removeSentence":
                    if (reduceSentenceKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (reduceSentenceKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (reduceSentenceKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (reduceSentenceKeyMod1.Equals("0"))
                        result1 = true;

                    if (reduceSentenceKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (reduceSentenceKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (reduceSentenceKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (reduceSentenceKeyMod2.Equals("0"))
                        result2 = true;
                    break;

                case "extendParagraph":
                    if (extendParagraphKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (extendParagraphKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (extendParagraphKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (extendParagraphKeyMod1.Equals("0"))
                        result1 = true;

                    if (extendParagraphKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (extendParagraphKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (extendParagraphKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (extendParagraphKeyMod2.Equals("0"))
                        result2 = true;
                    break;

                case "removeParagraph":
                    if (reduceParagraphKeyMod1.Equals("CTRL"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (reduceParagraphKeyMod1.Equals("ALT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (reduceParagraphKeyMod1.Equals("SHIFT"))
                        result1 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (reduceParagraphKeyMod1.Equals("0"))
                        result1 = true;

                    if (reduceParagraphKeyMod2.Equals("CTRL"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));
                    else if (reduceParagraphKeyMod2.Equals("ALT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LALT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RALT)));
                    else if (reduceParagraphKeyMod2.Equals("SHIFT"))
                        result2 = (Convert.ToBoolean(GetAsyncKeyState(VK_LSHIFT)) || Convert.ToBoolean(GetAsyncKeyState(VK_RSHIFT)));
                    else if (reduceParagraphKeyMod2.Equals("0"))
                        result2 = true;
                    break;
            }

            if (result1 && result2)
                result = true;

            return result;
        }
        #endregion

        #region Caret Position
        private Point evaluateCaretPosition()
        {
            Point caretPosition = new Point();

            getCaretPosition();

            caretPosition.X = (int)guiInfo.rcCaret.Left;//+ 25;
            caretPosition.Y = (int)guiInfo.rcCaret.Bottom;// +25;

            ClientToScreen(guiInfo.hwndCaret, out caretPosition);

            return caretPosition;
        }

        public void getCaretPosition()
        {
            GUITHREADINFO temp = new GUITHREADINFO();
            temp.cbSize = (uint)Marshal.SizeOf(temp);

            // Get GuiThreadInfo into guiInfo
            GetGUIThreadInfo(0, out temp);
            
            if(temp.rcCaret.Left != 0)
                guiInfo = temp;
        }
        #endregion

        #region Request Suggestion
        private bool handleRequestSuggestionMode(KbDllHookStruct kbDllHookStruct)
        {
            // Keep track of keyboard key 0-9, a-z and ' "
            // For the full list of key covers, please refer to http://www.kbdedit.com/manual/low_level_vk_list.html
            int VK_KEY_0 = 0x30;
            int VK_KEY_Z = 0x5A;
            int VK_OEM_7 = 0xDE; // OEM_7 (" ')
            int VK_OEM_1 = 0xBA; // OEM_1 (: ;)
            int VK_SPACE = 0x20;
            int VK_BACK = 0x08;

            int VK_LCONTROL = 0xA2;
            int VK_RCONTROL = 0xA3;
            int VK_LMENU = 0xA4; // left atrl
            int VK_RMENU = 0xA5; // right atrl
            int VK_LSHIFT = 0xA0;
            int VK_RSHIFT = 0xA1;

            if (autoCompleteForm.Visible)
            {
                bool isCTRLKey = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)));

                if (isCTRLKey)
                {
                    int[] keys = {(int)Keys.D1, (int)Keys.D2, (int)Keys.D3, (int)Keys.D4, (int)Keys.D5,
                                     (int)Keys.D6, (int)Keys.D7, (int)Keys.D8, (int)Keys.D9};

                    for (int i = 0; i < autoCompleteForm.getDisplaySuggestionCount(); i++)
                    {
                        if (kbDllHookStruct.vkCode == keys[i])
                        {
                            autoCompleteForm.quickPaste(i);
                            return false;
                        }
                    }
                }

                // Allow user to interact with the form when the pop-up is displaying.
                if (kbDllHookStruct.vkCode == (int)Keys.Down || kbDllHookStruct.vkCode == (int)Keys.Up)
                {
                    autoCompleteForm.selectFirstIndex();
                    return false; // block key
                }
                if (kbDllHookStruct.vkCode == (int)Keys.Right)
                {
                    autoCompleteForm.nextSuggestionPage();
                    return false; // block key
                }
                if (kbDllHookStruct.vkCode == (int)Keys.Left)
                {
                    // With this check, it allow user to press left button on Document when there is no previous page
                    if (autoCompleteForm.getPageIndex() != 1)
                    {
                        autoCompleteForm.previousSuggestionPage();
                        return false; // block key
                    }
                }
                if (kbDllHookStruct.vkCode == (int)Keys.Escape)
                {
                    autoCompleteForm.Hide();
                    return false; // block key
                }
            }

            bool isValidKeyTrigger = (kbDllHookStruct.vkCode >= VK_KEY_0 && kbDllHookStruct.vkCode <= VK_KEY_Z) || kbDllHookStruct.vkCode == VK_OEM_7
                || kbDllHookStruct.vkCode == VK_OEM_1 || kbDllHookStruct.vkCode == VK_SPACE || kbDllHookStruct.vkCode == VK_BACK || kbDllHookStruct.vkCode == (int)Keys.OemPeriod
                || kbDllHookStruct.vkCode == (int)Keys.Oem2;

            // Special keys here refer to ALT and CTRL key
            bool isSpecialKeyPressed = (Convert.ToBoolean(GetAsyncKeyState(VK_LCONTROL)) || Convert.ToBoolean(GetAsyncKeyState(VK_RCONTROL)) ||
                Convert.ToBoolean(GetAsyncKeyState(VK_LMENU)) || Convert.ToBoolean(GetAsyncKeyState(VK_RMENU)));

            if (isValidKeyTrigger && !isSpecialKeyPressed && Mode == TriggerMode.AUTO_TRIGGER)
            {

                autoCompleteForm.Hide();

                // Fetch GUITHREADINFO to prepare for evaluateCaretPosition() when the triggerSuggTimer elapse.
                getCaretPosition();

                if (triggerSuggTimer == null)
                {
                    triggerSuggTimer = new System.Timers.Timer();
                    triggerSuggTimer.Elapsed += new ElapsedEventHandler(delegate(Object sender, ElapsedEventArgs e)
                    {
                        triggerSuggTimer.Stop();
                        triggerSuggestion(true);

                        // Logging for user testing
                        startReqSuggTime = DateTime.Now;
                    });
                }

                triggerSuggTimer.Interval = TriggerDelay;
                triggerSuggTimer.Stop();
                triggerSuggTimer.Start();
            }
            else if ((kbDllHookStruct.vkCode == triggerSuggestionPopUpKey) && checkHotkeysModifiers("triggerSuggestionPopUp"))
            {
                getCaretPosition();
                triggerSuggestion(false);
            }
            else if (kbDllHookStruct.vkCode != VK_LCONTROL && kbDllHookStruct.vkCode != VK_RCONTROL && kbDllHookStruct.vkCode != VK_LSHIFT
                && kbDllHookStruct.vkCode != VK_RSHIFT)
            {
                autoCompleteForm.Hide();
            }

            return true;
        }

        private void triggerSuggestion(bool auto)
        {
            // initialize the coordinates to pop-up the suggestion box
            //evaluateCaretPosition();

            // Create a background worker thread to invoke requestSuggestion() method.
            // After the background worker thread is completed, it will call displaySuggestion() method
            // to display the result.
            MethodInvoker workerThread = null;

            // MethodInvoker can only take in method without parameter, hence we need to create two different methods
            // to separate the two modes.
            if (auto)
                workerThread = new MethodInvoker(requestSuggestionAutomatic);
            else
                workerThread = new MethodInvoker(requestSuggestionManual);

            workerThread.BeginInvoke(null, null);
        }

        private void requestSuggestionManual()
        {
            requestSuggestion(false);
        }

        private void requestSuggestionAutomatic()
        {
            requestSuggestion(true);
        }

        private void requestSuggestion(bool auto)
        {
            try
            {
                // Send network request to Java ACP to request for suggestions
                List<Suggestion> suggestions = null;

                String wordsToSend = getWordsBeforeLastSentence();
                wordsToSend = wordsToSend.TrimStart();
                
                if (!wordsToSend.Trim().Equals(""))
                {
                    suggestions = logic.requestSuggestion(wordsToSend, auto);
                }

                // Inform UI Thread to display suggestions
                Globals.ThisAddIn.Dispatcher.Invoke(new callMainThreadDisplaySuggestionDelegate(callMainThreadDisplaySuggestion), new Object[] { suggestions, auto });
            }
            catch (System.Net.Sockets.SocketException e)
            {
                // Inform UI Thread to display error message
                if (isEnabled)
                {
                    markServerDown();
                    Globals.ThisAddIn.Dispatcher.Invoke(new displayMessageDelegate(displayMessage), new Object[] { ERROR_CONNECTION });
                }
            }
        }

        // To allow background worker thread to display the suggestion box on the UI thread
        private delegate void callMainThreadDisplaySuggestionDelegate(List<Suggestion> suggestions, bool auto);
        private void callMainThreadDisplaySuggestion(List<Suggestion> suggestions, bool auto)
        {
            if (suggestions != null && suggestions.Count != 0)
            {
                autoCompleteForm.populateForm(suggestions);

                displaySuggestionBox();
            }
            else if (suggestions != null && !auto)
            {
                notificationForm.setMessage("No suggestions available");
                notificationForm.updateLocation(applicationLocation, applicationSize);
                notificationForm.showWithTimer(3);
            }
            else if (suggestions == null || auto)
            {
                autoCompleteForm.Hide();
            }
        }

        // To allow background worker threads to display error message
        private delegate void displayMessageDelegate(string msg);
        private void displayMessage(string msg)
        {
            notificationForm.setMessage(msg);
            notificationForm.updateLocation(applicationLocation, applicationSize);
            notificationForm.showWithTimer(5);
        }

        public void insertSuggestion(Suggestion suggestion)
        {
            // for User Testing
            endReqSuggTime = DateTime.Now;
            appendReqSuggTimeString();

            Word.Range range = currentSelection.Range;

            int characterCount = 0;

            switch (suggestion.type)
            {
                case Suggestion.SENTENCE:
                    // Find out the number of words before last sentence
                    characterCount = getWordsBeforeLastSentence().TrimStart().Count();
                    break;
                case Suggestion.ENTITY:
                    // Replace the last few word(s)
                    characterCount = checkEntityWords((Entity)suggestion);
                    break;
            }

            // Replace the text before last sentence with the choosen suggestion
            range = currentSelection.Range;
            extMode.setExtensionRange(range);
            range.MoveStart(Word.WdUnits.wdCharacter, -characterCount);
            switch (suggestion.type)
            {
                case Suggestion.SENTENCE:
                    range.Text = ((Sentence)suggestion).content + ExtensionMode.extraSpace;
                    break;
                case Suggestion.ENTITY:
                    range.Text = ((Entity)suggestion).content;
                    break;
            }

            // Reposition the cursor to the end of the sentence that is just pasted
            int position = range.End;
            currentSelection.SetRange(position, position);

            // Hide the form
            autoCompleteForm.Hide();

            if (suggestion.type == Suggestion.SENTENCE)
            {
                ExtensionMode.highlight(range);
                extMode.setupExtensionMode(suggestion);

                extendSuggestionForm.updateLocation(applicationLocation, applicationSize);
                extendSuggestionForm.ShowForm();
            }

            // Inform ACP the suggestion that was selected for Ranking purpose.
            logic.chooseSuggestion(suggestion);
        }

        private int checkEntityWords(Entity entity)
        {
            Word.Range range = currentSelection.Range;
            range.Start = range.End;
            string result = "";
            String[] words = entity.content.Split(' ');

            string temp = "";
            bool isCharBeforeLastSpace = false;

            for (int i = 0; i < words.Length; i++)
            {
                string lastWord = "";

                do
                {
                    // Move the start range index by one letter (left) 
                    // This is to check the word before the current cursor
                    range.MoveStart(Word.WdUnits.wdCharacter, -1);
                    temp = range.Text;

                    isCharBeforeLastSpace = (temp != null) && (!temp.Equals(" ") && (!temp.Equals("\r")));

                    if (isCharBeforeLastSpace)
                        lastWord = temp + lastWord;

                    // Move the end range index by one word (left)
                    range.MoveEnd(Word.WdUnits.wdCharacter, -1);
                } while (isCharBeforeLastSpace);

                if (i == 0)
                {
                    result = lastWord + " " + result;
                }
                else if (lastWord != null)
                {
                    for (int j = 0; j < words.Length - 1; j++)
                    {
                        if (words[j].Trim().ToLower().Equals(lastWord.ToLower().Trim()))
                        {
                            result = lastWord + " " + result;
                            break;
                        }
                    }
                }
            }

            return result.Trim().Count();
        }

        public String getRangeTextByWords(int numOfWords)
        {
            String result = "";
            Word.Range range = currentSelection.Range;
            range.Start = range.End;

            for (int i = 0; i < numOfWords; i++)
            {
                range.MoveStartUntil(" ", Word.WdConstants.wdBackward);
                result = range.Text + " " + result;

                range.MoveEndUntil(" ", Word.WdConstants.wdBackward);
                range.MoveEnd(Word.WdUnits.wdCharacter, -1);
            }

            return result.Trim();
        }

        public string getWordsBeforeLastSentence()
        {
            string lastSentenceString = "";

            Word.Range range = Globals.ThisAddIn.Application.Selection.Range;
            range.Start = range.End;

            string temp = "";
            bool isWordBeforeLastSentence = false;
            bool isSpecialKey = false;
            bool isNewLine = false;
            bool isSpaceForLastCharacter = false;
            bool isFullStop = false;

            String[] exceptionalWords = { "mr.", "ms.", "mrs.", "mdm.",  "prof.", "dr."};

            do
            {
                // Move the start range index by one letter (left) 
                // This is to check the word before the current cursor
                range.MoveStart(Word.WdUnits.wdCharacter, -1);
                temp = range.Text;

                if (temp != null)
                {
                    // Check if the word is before last sentence or at the beginning of the document (null)
                    isSpecialKey = (temp.Trim().Equals("!")
                        || temp.Trim().Equals("?")
                        || temp.Trim().Equals("."));
                    isNewLine = temp.Equals("\r");
                    isFullStop = temp.Trim().Equals(".");

                    isWordBeforeLastSentence = (!isNewLine && (!isSpecialKey || (isSpecialKey && !isSpaceForLastCharacter)));

                    if (isFullStop)
                    {
                        range.MoveStart(Word.WdUnits.wdWord, -1);
                        temp = range.Text;

                        for (int i = 0; i<exceptionalWords.Count(); i++)
                        {
                            if(temp.ToLower().Equals(exceptionalWords[i].ToLower())){
                                isWordBeforeLastSentence = true;
                                break;
                            }
                        }
                        range.End = range.Start;
                        range.MoveEnd(Word.WdUnits.wdCharacter, 1);
                    }
                }
                else
                {
                    isWordBeforeLastSentence = false;
                }

                if (isWordBeforeLastSentence)
                    lastSentenceString = temp + lastSentenceString;

                // Move the end range index by one letter (left)
                range.MoveEnd(Word.WdUnits.wdCharacter, -1);

                if (temp != null && temp.Equals(" "))
                {
                    isSpaceForLastCharacter = true;
                }
                else
                {
                    isSpaceForLastCharacter = false;
                }
            } while (isWordBeforeLastSentence);

            return lastSentenceString;
        }

        // Display suggestion box on screen
        private void displaySuggestionBox()
        {
            Word.Selection currentSelection = Application.Selection;

            currentSelection = Application.Selection;

            Point caretPosition = evaluateCaretPosition();
            caretPosition = calculateSuggestionBoxPosition(caretPosition);
            autoCompleteForm.SetDesktopLocation(caretPosition.X, caretPosition.Y);
            autoCompleteForm.Visible = true;
        }

        // Calculate the correct caret position such that the suggestion box will not go off screen
        private Point calculateSuggestionBoxPosition(Point caretPosition)
        {
            if (caretPosition.X + autoCompleteForm.getActualWidth() > Screen.PrimaryScreen.Bounds.Width)
            {
                caretPosition.X = Screen.PrimaryScreen.Bounds.Width - autoCompleteForm.getActualWidth();
            }
            if (caretPosition.Y + autoCompleteForm.Height > Screen.PrimaryScreen.Bounds.Height)
            {
                // TODO: Must consider the current font size as well
                caretPosition.Y = caretPosition.Y - autoCompleteForm.Height - 20;
            }

            return caretPosition;
        }
        #endregion

        #region Extension Mode
        private bool handleExtensionMode(KbDllHookStruct kbDllHookStruct)
        {
            int code = -1;

            if (kbDllHookStruct.vkCode == (int)Keys.Space || kbDllHookStruct.vkCode == (int)Keys.Enter)
            {
                extMode.resetExtensionMode();
                return false;
            }
            else if (kbDllHookStruct.vkCode == (int)Keys.Back)
            {
                extMode.removeRangeTextAndRepositionCursor(extMode.getExtensionRange());
                extMode.resetExtensionMode();
                return false;
            }
            else
            {
                code = checkKeyCombination(kbDllHookStruct);

                // if is correct key combination and is extension mode
                if (code > -1 && extMode.getExtensionPos() != -1)
                {
                    if (extensionBw == null)
                    {
                        extensionBw = new BackgroundWorker();
                        extensionBw.DoWork += extensionBgWork;
                    }

                    if (!extensionBw.IsBusy)
                        extensionBw.RunWorkerAsync(code);

                    return false;
                }
                else if (kbDllHookStruct.vkCode == (int)Keys.LControlKey || kbDllHookStruct.vkCode == (int)Keys.RControlKey ||
                    kbDllHookStruct.vkCode == (int)Keys.LMenu || kbDllHookStruct.vkCode == (int)Keys.RMenu ||
                    kbDllHookStruct.vkCode == (int)Keys.LShiftKey || kbDllHookStruct.vkCode == (int)Keys.RShiftKey)
                {
                    return true;
                }
                else
                {
                    if (extensionBw == null || !extensionBw.IsBusy)
                    {
                        extendSuggestionForm.fadeOut();
                        extMode.resetExtensionMode();
                    }
                }
            }

            return true;
        }

        private int checkKeyCombination(KbDllHookStruct kbDllHookStruct)
        {
            int code = -1;

            // check key combination
            if (kbDllHookStruct.vkCode == extendParagraphKey && checkHotkeysModifiers("extendParagraph"))
                code = ExtensionMode.EXTENDPARAGRAPH;
            else if (kbDllHookStruct.vkCode == reduceParagraphKey && checkHotkeysModifiers("removeParagraph"))
                code = ExtensionMode.REMOVEPARAGRAPH;
            else if (kbDllHookStruct.vkCode == extendSentenceKey && checkHotkeysModifiers("extendSentence"))
                code = ExtensionMode.EXTENDSENTENCE;
            else if (kbDllHookStruct.vkCode == reduceSentenceKey && checkHotkeysModifiers("removeSentence"))
                code = ExtensionMode.REMOVESENTENCE;
            else if (kbDllHookStruct.vkCode == extendWordKey && checkHotkeysModifiers("extendWord"))
                code = ExtensionMode.EXTENDWORD;
            else if (kbDllHookStruct.vkCode == reduceWordKey && checkHotkeysModifiers("removeWord"))
                code = ExtensionMode.REMOVEWORD;

            return code;
        }

        private void extensionBgWork(object sender, DoWorkEventArgs e)
        {
            while (Interlocked.CompareExchange(ref extMode.isRetrieving, 0, 1) == 0)
                Thread.Sleep(10);

            extMode.extendSuggestion((int)e.Argument);
        }

        #endregion

        #region Getters Method
        public Notification getNotificationForm()
        {
            return notificationForm;
        }

        public Point getApplicationLocation()
        {
            return applicationLocation;
        }

        public Size getApplicationSize()
        {
            return applicationSize;
        }
        #endregion

        #region Logging Key Stroke
        private void appendLogString(int vkCode)
        {
            if (isUserActionLogging)
            {
                String currentTime = DateTime.Now.ToString("HH:mm:ss");

                bool hasASyncKey = false;
                String aSyncKey = "";

                if ((Convert.ToBoolean(GetAsyncKeyState((int)Keys.LControlKey)) || Convert.ToBoolean(GetAsyncKeyState((int)Keys.RControlKey))))
                {
                    hasASyncKey = true;
                    aSyncKey = "CTRL";
                }
                else if ((Convert.ToBoolean(GetAsyncKeyState((int)Keys.LMenu))) || Convert.ToBoolean(GetAsyncKeyState((int)Keys.RMenu)))
                {
                    hasASyncKey = true;
                    aSyncKey = "ALT";
                }
                else if ((Convert.ToBoolean(GetAsyncKeyState((int)Keys.LShiftKey)) || Convert.ToBoolean(GetAsyncKeyState((int)Keys.RShiftKey))))
                {
                    hasASyncKey = true;
                    aSyncKey = "SHIFT";
                }

                if (hasASyncKey)
                {
                    if (vkCode != (int)Keys.LControlKey && vkCode != (int)Keys.RControlKey &&
                            vkCode != (int)Keys.LMenu && vkCode != (int)Keys.RMenu &&
                            vkCode != (int)Keys.LShiftKey && vkCode != (int)Keys.RShiftKey)
                    {
                        logContent += aSyncKey + " + " + ((Keys)vkCode).ToString() + ",," + currentTime + "\r";
                        Debug.WriteLine(aSyncKey + " + " + ((Keys)vkCode).ToString() + ",," + currentTime + "\r");
                    }
                }
                else
                {
                    logContent += ((Keys)vkCode).ToString() + ",," + currentTime + "\r";
                    Debug.WriteLine(((Keys)vkCode).ToString() + ",," + currentTime + "\r");
                }
            }
        }

        private void appendSwitchWindowString()
        {
            TimeSpan span = endSwitchTime.Subtract(startSwitchTime);
            int totalTime = span.Minutes * 60 + span.Seconds;

            String startSwitchTimeStr = startSwitchTime.ToString("HH:mm:ss");

            if (span.Hours == 0 && span.Seconds > 0)
            {
                if (isEnabled)
                {
                    logContent += "SWITCH WINDOW AUTO," + totalTime + "," + startSwitchTimeStr + "\r";
                    Debug.WriteLine("SWITCH WINDOW AUTO," + totalTime + "," + startSwitchTimeStr + "\r");
                }
                else
                {
                    logContent += "SWITCH WINDOW MANUAL," + totalTime + "," + startSwitchTimeStr + "\r";
                    Debug.WriteLine("SWITCH WINDOW MANUAL," + totalTime + "," + startSwitchTimeStr + "\r");
                }
            }
        }

        private void appendReqSuggTimeString()
        {
            TimeSpan span = endReqSuggTime.Subtract(startReqSuggTime);
            int totalSecs = span.Minutes * 60 + span.Seconds;

            String endReqSuggTimeStr = endReqSuggTime.ToString("HH:mm:ss");

            logContent += "REQ SUGG TIME," + totalSecs + "," + endReqSuggTimeStr + "\r";
            Debug.WriteLine("REQ SUGG TIME," + totalSecs + "," + endReqSuggTimeStr + "\r");
        }

        #endregion

        #region File I/O
        private void writeToFile(String content)
        {
            content = startTestTime + endTestTime + content;
            Debug.WriteLine(content);

            StreamWriter OurStream;
            using (OurStream = new StreamWriter("C:\\Windows\\Temp\\AutoComPaste\\user_testing.csv"))
            {
                OurStream.Write(content);
            }
            
            OurStream.Close();

        }
        #endregion
    }
}
