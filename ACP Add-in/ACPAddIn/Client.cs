using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.IO;
using ACPAddIn.Object_Class;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Windows.Forms;

namespace ACPAddIn
{
    /// <summary>
    /// This class hide the underlying network components to the upper layer. It provide a set of logic function to call,
    /// and each call will trigger a network request to call the same function in Java ACP under ACPLogic class.
    /// 
    /// Author: Loke Yan Hao
    /// </summary>
    class Client
    {
        public const String host = "127.0.0.1";
        public const int port = 5566;

        public List<Suggestion> requestSuggestion(String userInput, Boolean autoTrigger)
        {
            List<Object> parameters = new List<Object>();
            parameters.Add(userInput);
            parameters.Add(autoTrigger);
            Operation operation = new Operation("requestSuggestion", parameters);

            OperationResult result = invokeRemote(operation);

            List<Suggestion> suggestions = new List<Suggestion>();

            if (result != null)
            {
                JArray resultArray = (JArray)result.reply; 
                for (int i = 0; i < resultArray.Count; i++)
                {
                    Suggestion suggestion = resultArray[i].ToObject<Suggestion>();
                    if (suggestion != null)
                    {
                        switch (suggestion.type)
                        {
                            case Suggestion.SENTENCE:
                                suggestions.Add(resultArray[i].ToObject<Sentence>());
                                break;
                            case Suggestion.ENTITY:
                                suggestions.Add(resultArray[i].ToObject<Entity>());
                                break;
                        }
                    }

                }
            }

            return suggestions;
        }

        public void chooseSuggestion(Suggestion suggestion)
        {
            List<Object> parameters = new List<Object>();
            parameters.Add(suggestion);
            Operation operation = new Operation("chooseSuggestion", parameters);

            OperationResult result = invokeRemote(operation);
        }

        public void setDestinationDocument(int id)
        {
            List<Object> parameters = new List<Object>();
            parameters.Add(id);
            Operation operation = new Operation("setDestinationDocument", parameters);

            if (id != 0)
            {
                OperationResult result = invokeRemote(operation);
            }
        }

        public void closeDestinationDocument(int id)
        {
            List<Object> parameters = new List<Object>();
            parameters.Add(id);
            Operation operation = new Operation("closeDestinationDocument", parameters);

            OperationResult result = invokeRemote(operation);
        }

        public String getFilePath()
        {
            List<Object> parameters = new List<Object>();
            Operation operation = new Operation("getFilePath", parameters);

            OperationResult result = invokeRemote(operation);
            String filePath = (String) result.reply;

            return filePath;
        }

        public List<Suggestion> requestExtendSuggestion(int id, int type)
        {
            List<Object> parameters = new List<Object>();
            parameters.Add(id);
            parameters.Add(type);
            Operation operation = new Operation("requestExtendSuggestion", parameters);

            OperationResult result = invokeRemote(operation);

            List<Suggestion> extensions = new List<Suggestion>();

            if (result != null)
            {
                JArray resultArray = (JArray)result.reply;
                for (int i = 0; i < resultArray.Count; i++)
                {
                    Suggestion suggestion = resultArray[i].ToObject<Suggestion>();
                    if (suggestion != null)
                    {
                        extensions.Add(resultArray[i].ToObject<Sentence>());
                    }

                }
            }

            return extensions;
        }

        public bool checkServerAlive()
        {
            bool result  = false;

            TcpClient clientSocket = new TcpClient();
            try
            {
                clientSocket.Connect(host, port);

                NetworkStream serverStream = clientSocket.GetStream();
                StreamWriter clientStreamWriter = new StreamWriter(serverStream);
                StreamReader clientStreamReader = new StreamReader(serverStream);

                clientStreamWriter.WriteLine("GET STATUS");
                clientStreamWriter.Flush();

                String replyJson = clientStreamReader.ReadLine();
                if (replyJson != null)
                {
                    OperationResult oResult = JsonConvert.DeserializeObject<OperationResult>(replyJson);
                    if (oResult.status == OperationResult.REPLY)
                    {
                        result = true;
                    }
                }

                clientStreamReader.Close();
                clientStreamWriter.Close();
            }
            catch (SocketException e)
            {
                result = false;
            }
            finally
            {
                clientSocket.Close();
            }

            return result;
        }

        private OperationResult invokeRemote(Operation operation)
        {
            OperationResult result = null;
            TcpClient clientSocket = new TcpClient();
            try
            {
                clientSocket.Connect(host, port);

                NetworkStream serverStream = clientSocket.GetStream();
                StreamWriter clientStreamWriter = new StreamWriter(serverStream);
                StreamReader clientStreamReader = new StreamReader(serverStream);

                String jsonOperation = JsonConvert.SerializeObject(operation);

                clientStreamWriter.WriteLine(jsonOperation);
                clientStreamWriter.Flush();

                String replyJson = clientStreamReader.ReadLine();
                if (replyJson != null)
                {
                    result = JsonConvert.DeserializeObject<OperationResult>(replyJson);
                }

                clientStreamReader.Close();
                clientStreamWriter.Close();
            }
            catch (SocketException e)
            {
                throw e;
            }
            finally
            {
                clientSocket.Close();
            }

            return result;
        }
    }
}
