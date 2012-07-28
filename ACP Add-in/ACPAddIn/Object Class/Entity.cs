using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ACPAddIn.Object_Class
{
    /// <summary>
    /// This class contain the same data structure for the Entity object in Java ACP.
    /// It is used for to convert between JSON and object.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    public class Entity : Suggestion
    {
        public const int EMAIL = 201;
        public const int ADDRESS = 202;
        public const int PLACE = 203;
        public const int URL = 204;
        public const int NAME = 205;

        public int entityType;
        public String content;
    }
}
