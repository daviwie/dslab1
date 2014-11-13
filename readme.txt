Reflect about your solution!

Description of controller package: 
- container: contains a container for Node and a container for User. These contain the various attributes relevant to a User (Client) or a Node.
- handler: contains a ClientHandler that processes all client input in its own thread
- listener: ClientListener listens on a TCP port for any incoming connections and creates a ClientHandler for each new connection and runs each handler in its own thread. NodeUDPListener receives all DatagramPackets from the Nodes and builds the alive list.
- persistence: contains custom ConcurrentHashMaps for Nodes and Users that save the NodeData and UserData and provide some custom methods for each
- timer: checks to see if any nodes have gone offline

Description of node package:
- container: similar to the Node container in CloudController, this class contains the attributes relevant for a node to make exchanging of data easier
- handler: processes all input from the ClientHandler, which is managed by the CloudController
- listener: listens for any incoming TCP requests from the ClientHandler and creates a ControllerHandler to process the input
- logger: outputs log files of each node's respective activities
- timer: sends DatagramPackets via UDP to the CloudController at regular intervals to inform the CloudController it is alive and on what TCP port it is listening