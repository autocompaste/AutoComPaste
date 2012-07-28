﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ACPAddIn
{
    /// <summary>
    /// This class contain the mapping from a hotkey text string, to the virtual key code.
    /// Please refer to http://www.kbdedit.com/manual/low_level_vk_list.html.
    /// 
    /// Author: Ng Chin Hui
    /// </summary>
    class KeyboardMapping
    {
        public static Dictionary<string, int> Keys = new Dictionary<string, int>()
        {
            {"ABNT_C1", 0xC1},
            {"ABNT_C2", 0xC2},
            {"NUMPAD_PLUS", 0x6B},
            {"ATTN", 0xF6},
            {"BACKSPACE", 0x08},
            {"BREAK", 0x03},
            {"CLEAR", 0x0C},
            {"CRSEL", 0xF7},
            {"NUMPAD_DECIMAL", 0x6E},
            {"NUMPAD_DIVIDE", 0x6F},
            {"EREOF", 0xF9},
            {"ESCAPE", 0x1B},
            {"EXECUTE", 0x2B},
            {"EXSEL", 0xF8},
            {"ICO_CLEAR", 0xE6},
            {"ICO_HELP", 0xE3},
            {"0", 0x30},
            {"1", 0x31},
            {"2", 0x32},
            {"3", 0x33},
            {"4", 0x34},
            {"5", 0x35},
            {"6", 0x36},
            {"7", 0x37},
            {"8", 0x38},
            {"9", 0x39},
            {"A", 0x41},
            {"B", 0x42},
            {"C", 0x43},
            {"D", 0x44},
            {"E", 0x45},
            {"F", 0x46},
            {"G", 0x47},
            {"H", 0x48},
            {"I", 0x49},
            {"J", 0x4A},
            {"K", 0x4B},
            {"L", 0x4C},
            {"M", 0x4D},
            {"N", 0x4E},
            {"O", 0x4F},
            {"P", 0x50},
            {"Q", 0x51},
            {"R", 0x52},
            {"S", 0x53},
            {"T", 0x54},
            {"U", 0x55},
            {"V", 0x56},
            {"W", 0x57},
            {"X", 0x58},
            {"Y", 0x59},
            {"Z", 0x5A},
            {"NUMPAD_MULTIPLY", 0x6A},
            {"NONAME", 0xFC},
            {"NUMPAD_0", 0x60},
            {"NUMPAD_1", 0x61},
            {"NUMPAD_2", 0x62},
            {"NUMPAD_3", 0x63},
            {"NUMPAD_4", 0x64},
            {"NUMPAD_5", 0x65},
            {"NUMPAD_6", 0x66},
            {"NUMPAD_7", 0x67},
            {"NUMPAD_8", 0x68},
            {"NUMPAD_9", 0x69},
            {"SEMICOLON", 0xBA},
            {"OEM_102", 0xE2},
            {"SLASH", 0xBF},
            {"BACK QUOTE", 0xC0},
            {"OPEN BRACKET", 0xDB},
            {"BACK SLASH", 0xDC},
            {"CLOSE BRACKET", 0xDD},
            {"QUOTE", 0xDE},
            {"OEM_8", 0xDF},
            {"OEM_ATTN", 0xF0},
            {"OEM_AUTO", 0xF3},
            {"OEM_AX", 0xE1},
            {"OEM_BACKTAB", 0xF5},
            {"OEM_CLEAR", 0xFE},
            {"COMMA", 0xBC},
            {"OEM_COPY", 0xF2},
            {"OEM_CUSEL", 0xEF},
            {"OEM_ENLW", 0xF4},
            {"OEM_FINISH", 0xF1},
            {"OEM_FJ_LOYA", 0x95},
            {"OEM_FJ_MASSHOU", 0x93},
            {"OEM_FJ_ROYA", 0x96},
            {"OEM_FJ_TOUROKU", 0x94},
            {"OEM_JUMP", 0xEA},
            {"MINUS", 0xBD},
            {"OEM_PA1", 0xEB},
            {"OEM_PA2", 0xEC},
            {"OEM_PA3", 0xED},
            {"PERIOD", 0xBE},
            {"EQUALS", 0xBB},
            {"OEM_RESET", 0xE9},
            {"OEM_WSCTRL", 0xEE},
            {"PA1", 0xFD},
            {"PACKET", 0xE7},
            {"PLAY", 0xFA},
            {"PROCESSKEY", 0xE5},
            {"ENTER", 0x0D},
            {"SELECT", 0x29},
            {"SEPARATOR", 0x6C},
            {"SPACE", 0x20},
            {"NUMPAD_SUBTRACT", 0x6D},
            {"TAB", 0x09},
            {"ZOOM", 0xFB},
            {"VK__none_", 0xFF},
            {"VK_ACCEPT", 0x1E},
            {"VK_APPS", 0x5D},
            {"VK_BROWSER_BACK", 0xA6},
            {"VK_BROWSER_FAVORITES", 0xAB},
            {"VK_BROWSER_FORWARD", 0xA7},
            {"VK_BROWSER_HOME", 0xAC},
            {"VK_BROWSER_REFRESH", 0xA8},
            {"VK_BROWSER_SEARCH", 0xAA},
            {"VK_BROWSER_STOP", 0xA9},
            {"VK_CAPITAL", 0x14},
            {"VK_CONVERT", 0x1C},
            {"VK_DELETE", 0x2E},
            {"DOWN", 0x28},
            {"VK_END", 0x23},
            {"F1", 0x70},
            {"F10", 0x79},
            {"F11", 0x7A},
            {"F12", 0x7B},
            {"F13", 0x7C},
            {"F14", 0x7D},
            {"F15", 0x7E},
            {"F16", 0x7F},
            {"F17", 0x80},
            {"F18", 0x81},
            {"F19", 0x82},
            {"F2", 0x71},
            {"F20", 0x83},
            {"F21", 0x84},
            {"F22", 0x85},
            {"F23", 0x86},
            {"F24", 0x87},
            {"F3", 0x72},
            {"F4", 0x73},
            {"F5", 0x74},
            {"F6", 0x75},
            {"F7", 0x76},
            {"F8", 0x77},
            {"F9", 0x78},
            {"VK_FINAL", 0x18},
            {"VK_HELP", 0x2F},
            {"VK_HOME", 0x24},
            {"VK_ICO_00", 0xE4},
            {"VK_INSERT", 0x2D},
            {"VK_JUNJA", 0x17},
            {"VK_KANA", 0x15},
            {"VK_KANJI", 0x19},
            {"VK_LAUNCH_APP1", 0xB6},
            {"VK_LAUNCH_APP2", 0xB7},
            {"VK_LAUNCH_MAIL", 0xB4},
            {"VK_LAUNCH_MEDIA_SELECT", 0xB5},
            {"VK_LBUTTON", 0x01},
            {"CTRL", 0xA2},
            {"LEFT", 0x25},
            {"ALT", 0xA4},
            {"SHIFT", 0xA0},
            {"VK_LWIN", 0x5B},
            {"VK_MBUTTON", 0x04},
            {"VK_MEDIA_NEXT_TRACK", 0xB0},
            {"VK_MEDIA_PLAY_PAUSE", 0xB3},
            {"VK_MEDIA_PREV_TRACK", 0xB1},
            {"VK_MEDIA_STOP", 0xB2},
            {"VK_MODECHANGE", 0x1F},
            {"VK_NEXT", 0x22},
            {"VK_NONCONVERT", 0x1D},
            {"VK_NUMLOCK", 0x90},
            {"VK_OEM_FJ_JISHO", 0x92},
            {"VK_PAUSE", 0x13},
            {"VK_PRINT", 0x2A},
            {"VK_PRIOR", 0x21},
            {"VK_RBUTTON", 0x02},
            {"VK_RCONTROL", 0xA3},
            {"RIGHT", 0x27},
            {"VK_RMENU", 0xA5},
            {"VK_RSHIFT", 0xA1},
            {"VK_RWIN", 0x5C},
            {"VK_SCROLL", 0x91},
            {"VK_SLEEP", 0x5F},
            {"VK_SNAPSHOT", 0x2C},
            {"UP", 0x26},
            {"VK_VOLUME_DOWN", 0xAE},
            {"VK_VOLUME_MUTE", 0xAD},
            {"VK_VOLUME_UP", 0xAF},
            {"VK_XBUTTON1", 0x05},
            {"VK_XBUTTON2", 0x06},
        };
    }
}