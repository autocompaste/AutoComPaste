using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ACPAddIn.Object_Class
{
    /// <summary>
    /// This class contain the same data structure for the OperationResult object in Java ACP.
    /// It is used for to convert between JSON and object.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>

    class OperationResult
    {
        public int status;
        public Object reply;

        public const int REPLY = 0;
	    public const int ERROR_REPLY = 1;

        public OperationResult(int status, Object reply)
        {
            this.status = status;
            this.reply = reply;
        }
    }
}
