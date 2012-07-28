using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ACPAddIn.Object_Class
{
    /// <summary>
    /// This class contain the same data structure for the Suggestion object in Java ACP.
    /// It is used for to convert between JSON and object.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    public class Suggestion
    {
        public const int SENTENCE = 101;
	    public const int PARAGRAPH = 102;
	    public const int ENTITY = 103;
        public const int WORD = 104;

        public int id;
        public int type;
        public Source source;
    }
}
