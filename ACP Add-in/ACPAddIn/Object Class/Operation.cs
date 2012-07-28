using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ACPAddIn.Object_Class
{
    /// <summary>
    /// This class contain the same data structure for the Operation object in Java ACP.
    /// It is used for to convert between JSON and object.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    class Operation
    {
        public String name;
        public List<Object> parameters;

        public Operation(String name, List<Object> parameters)
        {
            this.name = name;
            this.parameters = parameters;
        }
    }
}
