import com.google.gson.Gson;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

/**
 * 玩家服务线程
 */
class ClientThread extends Thread {
    private Client client = null;
    private Socket socket;
    private PrintStream sendStream;
    private BufferedReader getStream;
    private ServerGameRoom currentGameRoom;
    private JTextArea GuiShowMes;
    private MultiPlayTimeStop timeStop;
    private RandomReward randomReward;
    private boolean isConnected = false;//是否连接
    private boolean isLogin = false;//是否登陆

    /**
     * 获取此线程实例对象的Gamer
     *
     * @return
     */
    public Client getClient() {
        return client;
    }

    /**
     * 玩家服务线程构造
     *
     * @param socket 属于实例对象(一位玩家)的Socket通道
     */
    public ClientThread(Socket socket, PrintStream sendStream, BufferedReader getStream, JTextArea GuiShowMes) {
        this.socket = socket;
        this.sendStream = sendStream;//获取写出流
        this.getStream = getStream;//获取写入流
        this.GuiShowMes=GuiShowMes;//获得界面JTextarea对象
        System.out.println("成功创建一个玩家服务线程");
    }

    /**
     * 玩家服务线程run函数
     */
    public void run() {
        String line = null;//接收到的初始字符串（信息）
        String realMessage = null;//去除头部命令的信息
        //线程不被interrupted则持续接收玩家发来的信息
        Gson gson=new Gson();
        try {
        sign:while (!this.isInterrupted() &&(line=getStream.readLine())!=null)
        {
            if (isConnected)
            {
                    //TODO:服务线程run待完成
                if(!line.startsWith(Sign.CreateBullet )&& !line.startsWith(Sign.PlayerMove))
                    System.out.println("收到一个命令信息" + line);
                    /**
                     * 如果是登陆则采取如下操作
                     */
                    if (!isLogin && line.startsWith(Sign.Login))
                    {
                        try {
                            System.out.println("进度登陆函数");
                            int loginResult = check.checkLoginInfo(line);
                            System.out.println("登陆结果为" + loginResult);//1为成功 -1为账号未注册  为密码错误
                            switch (loginResult)
                            {
                                case 1:
                                    {
                                    boolean isRepeatLogin=false;
                                    realMessage=check.getRealMessage(line,Sign.Login);
                                    String id=realMessage.split(Sign.SplitSign)[0];
                                    isLogin = true;//密码成功则将当前玩家的服务线程登陆状态置为true
                                    for(Client c: StartServer.allPlayer)
                                    {
                                        if(c.getId().equals(id))
                                        {
                                           if(c.isOline())
                                           {
                                               //回复重复登陆的消息
                                               sendStream.println(Sign.RepeatLogin);
                                               isRepeatLogin=true;
                                           }
                                           else
                                           {
                                                client=c;
                                           }
                                        }
                                    }
                                    if(!isRepeatLogin)
                                    {
                                        client.setOline(true);//将玩家置为在线状态
                                        GuiShowMes.append("服务器消息：玩家： " + client.getId() + " 成登陆进入服务器。\n");//在guii显示登陆信息
                                        String clientStr = gson.toJson(client);//将登陆玩家序列化方便发送给其他玩家
                                        // 通知其他所有在线玩家该玩家上线
                                        for (PrintStream allsendstream : StartServer.clientPrintStreamMap.values()) {
                                            allsendstream.println(Sign.OneClientOnline + client.getId());
                                        }

                                        StartServer.onlineClients.add(client);//在在线玩家列表中加入玩家

                                        StartServer.clientPrintStreamMap.put(client, sendStream);//加入玩家写流
                                        System.out.println("发送序列化在线玩家列表。 大小为" + StartServer.onlineClients.size());
                                        //打包发送初始化消息
                                        String allclientsStr = gson.toJson(StartServer.onlineClients);
                                        String roomStr = gson.toJson(StartServer.allGameRoom);
                                        //打包发送
                                        sendStream.println(Sign.LoginSuccess);
                                        sendStream.println(allclientsStr + Sign.SplitSign + roomStr + Sign.SplitSign + clientStr);
                                    }
                                    break;
                                }
                                case -1: {
                                    sendCommand(Sign.IsNotRegistered);//返回账号还未注册的消息
                                    break;
                                }
                                case 0: {
                                    sendCommand(Sign.WrongPassword);//返回密码错误的消息
                                    break;
                                }
                            }
                        } catch (IOException e) {
                        }
                    }
                    /**
                     * 如果接收到注册请求
                     */
                    else if (!isLogin && line.startsWith(Sign.Register)) {
                        System.out.println("收到注册请求开始注册流程。");
                        //分割命令与内容
                        realMessage = check.getRealMessage(line, Sign.Register);
                        String playerid = realMessage.split(Sign.SplitSign)[0];
                        String playerPassword = realMessage.split(Sign.SplitSign)[1];
                        System.out.println("注册请求名字："+playerid);
                        if (!check.isRegistered(playerid)) {
                            System.out.println(StartServer.allPlayer.size());
                            AllInfo.addClient(new Client(playerid,playerPassword));//注册一个玩家到内存
                            System.out.println(StartServer.allPlayer.size());
                            sendCommand(Sign.RegisterSuccess);//返回注册成功信息
                            GuiShowMes.append("服务器消息：玩家："+playerid+" 成功注册。\n");//gui界面显示注册成功消息
                        } else sendCommand(Sign.IsRegistered);//否则返回已经注册过的消息
                    }
                    /**
                     * 如果收到创建房间信息
                     */
                    else if (isLogin && line.startsWith(Sign.CreateRoom)) {
                        System.out.println("收到创建房间请求。");
                        realMessage = check.getRealMessage(line, Sign.CreateRoom);
                        ServerGameRoom serverGameRoom = new ServerGameRoom(client.getId(), client, realMessage);//以玩家的id和对象还有发来的房间名字创建房间
                        System.out.println(client.getId());
                        System.out.println(realMessage);
                        StartServer.allGameRoom.add(serverGameRoom);
                        //设置当前玩家房间为自己创建的房间
                        client.setGameRoomID(serverGameRoom.getId());
                        //设置当前所在房间为
                        currentGameRoom=serverGameRoom;
                        String roomStr=gson.toJson(serverGameRoom);
                        System.out.println("成功创建名字为" + realMessage + "的房间。");
                        GuiShowMes.append("服务器消息：玩家："+client.getId()+" 成功创建名字为 "+client.getId()+" 的房间。\n");//gui界面显示成功创建房间信息
                        sendStream.println(Sign.PermissionCreateRoom +roomStr);
                        int i=0;
                        System.out.println("创建房间前map的大小"+StartServer.clientPrintStreamMap.size());
                        for (PrintStream sendstream : StartServer.clientPrintStreamMap.values()) {
                            System.out.println(i++);
                            sendstream.println(Sign.NewRoomCreate + roomStr);
                        }//发送给所有玩家房间创建信息
                        System.out.println("向所有玩家发送玩家房间信息创建标识。");
                    }
                    /**
                     * 如果收到加入房间信息
                     */
                    else if (isLogin && line.startsWith(Sign.EnterRoom))
                    {
                        realMessage = check.getRealMessage(line, Sign.EnterRoom);
                        String clientid = realMessage.split(Sign.SplitSign)[0];//获取创建者的名字与房间名字
                        String roomid = realMessage.split(Sign.SplitSign)[1]; //获取需要加入的房间名
                        ServerGameRoom serverGameRoom = null;
                        System.out.println("收到来自" + clientid + "加入" + roomid + "房间的请求");
                        System.out.println(StartServer.allGameRoom.size());
                        //找到房间
                        for (int i = 0; i < StartServer.allGameRoom.size(); i++)
                        {
                            if (StartServer.allGameRoom.get(i).getId().equals(roomid));
                            serverGameRoom = StartServer.allGameRoom.get(i);
                            break;
                        }
                        System.out.println("找到需要加入的房间名字为" + serverGameRoom.getId());
                        //如果房价没有满
                        if(serverGameRoom.getAllClients().size()<=4)
                        {
                        //转发给这房间内所有其他玩家
                        List<Client> list = serverGameRoom.getAllClients();
                            System.out.println("房间中的人数："+list.size());
                        for (Client c : list)
                        {
                            PrintStream sendstream;
                            //在房间内
                                sendstream = StartServer.clientPrintStreamMap.get(c);
                                sendstream.println(Sign.NewClientEnter + clientid);//转发给房间其他在线玩家xxx进入
                        }
                        System.out.println("开始转发给该房间其他玩家" + client.getId() + "加入了房间");
                        //将当前玩家加入到指定的房间内
                            //System.out.println("添加新的玩家到房间"+serverGameRoom.getId()+"的id"+client.getId()+"大小为"+serverGameRoom.getAllClients().size());
                        serverGameRoom.addClient(client);
                            //System.out.println("添加新的玩家到房间"+serverGameRoom.getId()+"的id"+client.getId()+"大小为"+serverGameRoom.getAllClients().size());
                            GuiShowMes.append("服务器消息：添加新的玩家到房间："+serverGameRoom.getId()+" 玩家的id为："+client.getId());
                        //将当前玩家所属房间指定为此房间
                        client.setGameRoomID(serverGameRoom.getId());
                        currentGameRoom=serverGameRoom;
                            System.out.println(serverGameRoom.getAllClients().size());
                        //服务端允许用户加入房间请求，并且发送房间对象序列化
                        String roomStr=gson.toJson(serverGameRoom);
                        sendStream.println(Sign.PermissionEnterRoom+roomStr);
                        }
                        //否则
                        else
                        {
                            System.out.println("返回房间已满");
                            sendStream.println(Sign.RoomFull);
                        }
                    }
                    /**
                     * 如果收到踢人的消息（房主可用）
                     */
                    else if (isLogin && line.startsWith(Sign.TickFromRoom)) {
                        realMessage = check.getRealMessage(line, Sign.TickFromRoom);
                        String targetId = realMessage.split(Sign.SplitSign)[0];//获取被T玩家id
                        String roomid = realMessage.split(Sign.SplitSign)[1];//获取房间ID
                        System.out.println("收到来自" + client.getId() + "的T人请求。");
                        if (client.equals(currentGameRoom.getMaster()))
                        {//如果为房主
                            List<Client> list = currentGameRoom.getAllClients();
                            for (Client c : list)
                            {
                                PrintStream printStream = StartServer.clientPrintStreamMap.get(c);
                                if (c.getId().equals(targetId))
                                {
                                    //发送给被T玩家被T信息
                                    printStream.println(Sign.BeenTicked);
                                    continue;
                                }
                                printStream.println(Sign.ClientLeaveRoom + targetId + Sign.SplitSign + roomid);

                            }
                            //发送给房间内所有玩家xxx被T除
                        }
                        //该房间移除该玩家(同时将该玩家的所属房间重新置空)
                        currentGameRoom.removeClient(targetId);
                    }
                    /**
                     * 如果收到离开房间的消息(房间内的人)
                     */
                    else if (isLogin && line.startsWith(Sign.LeaveRoom)) {
                        System.out.println("服务器收到" + client.getId() + "发来的离开房间的信息。");
                        leaveRoom();//离开房间
                    }
                    /**
                     * 如果收到注销请求(玩家返回到登陆界面)
                     */
                    else if (isLogin && line.startsWith(Sign.Logout))
                    {
                        System.out.println(client.getId()+"注销中，在线玩家列表大小为："+ StartServer.onlineClients.size());
                        sendStream.println(Sign.CloseLocalThread);
                        StartServer.clientPrintStreamMap.remove(client);
                        System.out.println("注销后map的大小"+StartServer.clientPrintStreamMap.size());
                        StartServer.onlineClients.remove(client);
                        System.out.println(client.getId()+"注销完成，在线玩家列表大小为："+ StartServer.onlineClients.size());
                        for(PrintStream sendStream: StartServer.clientPrintStreamMap.values())
                        {
                            sendStream.println(Sign.OneClientOffline+client.getId());
                        }
                        GuiShowMes.append("服务器消息：玩家："+client.getId()+" 下线。\n");
                        client.setOline(false);
                        client=null;
                        isLogin=false;
                        //设置当前玩家所在房间为空
                        currentGameRoom=null;
                        break sign;
                    }
                    /**
                     * 收到聊天信息命令
                     */
                    else if (isLogin && line.startsWith(Sign.SendPublicMessage)) {
                        realMessage = check.getRealMessage(line, Sign.SendPublicMessage);
                        String roomID = client.getRoomID();
                        //转发消息
                        for (Client c : currentGameRoom.getAllClients())
                        {
                            PrintStream printStream = StartServer.clientPrintStreamMap.get(c);
                            printStream.println(Sign.FromServerMessage  + client.getId() + ": " + realMessage);
                        }
                    }
                    /**
                     * 如果收到断开连接请求（返回到单人与多人游戏选择界面)
                     */
                    else if (line.startsWith(Sign.Disconnect)) {

                        stopThisClient( sendStream, getStream);
                        //关闭此服务线程 tips:原因：玩家请求断开连接退回到单人多人游戏选择界面
                    }
                    /**
                     * 如果收到开始游戏的命令
                     */
                    else if(line.startsWith(Sign.StartGame))
                    {
                            if(currentGameRoom.getMaster().equals(client))//如果发送开始游戏命令的玩家是房主
                            {
                                String roomId=client.getRoomID();
                                int playerNum=0;
                                for(ServerGameRoom serverGameRoom: StartServer.allGameRoom)
                                {
                                    if(serverGameRoom.getId().equals(roomId));
                                    playerNum=serverGameRoom.getAllClients().size();
                                }
                                //生成初始化出生地址的数组
                                Integer[] randomEntrance= AllInfo.randomArray(0,4,playerNum);
                                int i=0;
                                String randomStr=gson.toJson(randomEntrance);
                                for (Client c : currentGameRoom.getAllClients())//开始游戏并告知房间内其他所有人
                                {
                                    StartServer.clientPrintStreamMap.get(c).println(Sign.GameStart+randomStr+Sign.SplitSign+(i++));//为每位在房间内的玩家发送初始化的出生坐标

                                }

                                //为房主开始随机产生奖励线程
                                randomReward=new RandomReward(sendStream);
                                randomReward.start();
                                //设置定时游戏结束
                                timeStop=new MultiPlayTimeStop(currentGameRoom,randomReward,3000);
                                timeStop.start();
                                client.setPlaying(true);//设置当前玩家为正在对战状态
                                GuiShowMes.append("服务器消息：房间："+currentGameRoom.getId()+" 开始游戏。\n");
                            }
                    }
                    /**
                     * 如果收到客户端游戏启动准备完毕的消息
                     *
                     */
                    else if(line.startsWith(Sign.GameReadyStart))
                    {
                        client.setPlaying(true);
                        //为不是房主的玩家开始随机产生奖励线程
                        if(!currentGameRoom.getMaster().equals(client))
                        {
                            randomReward = new RandomReward(sendStream);
                            randomReward.start();
                        }
                    }
                /**
                 * 下面为游戏内服务的命令
                 */
                    /**
                     * 如果收到玩家移动的消息
                     */
                    else if(line.startsWith(Sign.PlayerMove))
                    {
                        if(client.isPlaying())
                        {
                            //直接获取玩家移动的消息
                            realMessage = line;
                            System.out.println(currentGameRoom.getAllClients().size());
                            for (Client c : currentGameRoom.getAllClients()) {
                                //直接转发给房间内所有的在线玩家
                                if (!c.equals(client)) {
                                    PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                    sendstream.println(realMessage);
                                }
                            }
                        }
                    }
                    /**
                     * 游戏内地雷爆炸的消息
                     */
                    else if (  line.startsWith(Sign.MineBoom))
                    {
                        if(client.isPlaying())
                        {
                            //获取爆炸的地雷的下标
                            realMessage = check.getRealMessage(line, Sign.MineBoom);
                            int mineflag = Integer.parseInt(realMessage.split(Sign.SplitSign)[0]);
                            //转发给房间内其他玩家
                            for (Client c : currentGameRoom.getAllClients())//给房间内所有玩家发送mineflag号地雷爆炸的消息
                            {
                                PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                sendstream.println(Sign.MineBoom + mineflag);//发送地雷爆炸消息
                            }
                        }
                    }
                /**
                 * 游戏内手雷爆炸消息
                 */
                    else if ( line.startsWith(Sign.GrenadeBoom))
                    {
                        if(client.isPlaying() ) {
                            //转发给房间内其他玩家
                            for (Client c : currentGameRoom.getAllClients())//给房间内所有玩家发送nadeflag号手雷爆炸的消息
                            {
                                PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                sendstream.println(line);//发送手雷爆炸消息
                            }
                        }
                    }
                    /**
                     * 游戏内玩家血量增加
                     */
                    else if(line.startsWith(Sign.AddHealthPoint))
                    {
                        if(client.isPlaying())
                        {
                            for (Client c : currentGameRoom.getAllClients()) {
                                //直接转发给房间内所有的在线玩家(除了自己)
                                if (!c.equals(client)) {
                                    PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                    sendstream.println(line);
                                }
                            }
                        }
                    }

                    /**
                     * 游戏内玩家开火消息
                      */
                    else if(line.startsWith(Sign.CreateBullet))
                    {
                        if(client.isPlaying()) {
                            for (Client c : currentGameRoom.getAllClients()) {
                                PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                System.out.println(sendstream==null);
                                sendstream.println(line);
                            }
                        }
                    }
                    /**
                     * 游戏内玩家放置地雷消息
                     */
                    else if(line.startsWith(Sign.CreateMine))
                    {
                        if(client.isPlaying()) {
                            for (Client c : currentGameRoom.getAllClients()) {
                                PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                sendstream.println(line);
                            }
                        }
                    }
                    /**
                     * 游戏内玩家丢出手雷消息
                     */
                    else if(line.startsWith(Sign.CreateGrenade))
                    {
                        if(client.isPlaying()) {
                            for (Client c : currentGameRoom.getAllClients()) {
                                PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                sendstream.println(line);
                            }
                        }
                    }
                    /**
                     * 玩家死亡消息
                     */
                    else if (  line.startsWith(Sign.OnePlayerDie))
                    {
                        if(client.isPlaying()) {
                            //获取爆炸的地雷的下标
                            realMessage = check.getRealMessage(line, Sign.OnePlayerDie);
                            int diePlayerFlag = Integer.parseInt(realMessage);
                            //转发给房间内其他玩家
                            for (Client c : currentGameRoom.getAllClients())//给房间内所有玩家发送flag玩家死亡的消息
                            {
                                PrintStream sendstream = StartServer.clientPrintStreamMap.get(c);
                                sendstream.println(line);//发送玩家死亡消息
                            }

                            //创建复活信息发送线程
                            new PlayerReliveThread(currentGameRoom, diePlayerFlag).start();
                        }
                    }
                /**
                 * 玩家中途离开游戏
                 */
                else if(line.startsWith(Sign.LeaveGame))
                {
                    if(client.isPlaying())
                    {
                        client.setPlaying(false);
                        //序列化初始化信息
                        String allclientsStr = gson.toJson(StartServer.onlineClients);
                        String roomStr = gson.toJson(StartServer.allGameRoom);
                        //如果退出的人是房主，则所有人都将退出游戏
                        if(currentGameRoom.getMaster().equals(client))
                        {
                            for(Client c:currentGameRoom.getAllClients())
                            {
                                StartServer.clientPrintStreamMap.get(c).println(Sign.GameOver+allclientsStr+Sign.SplitSign+roomStr);
                                c.setPlaying(false);
                            }
                        }
                        else
                        {
                            sendStream.println(Sign.GameOver + allclientsStr + Sign.SplitSign + roomStr);
                        }
                        int playeringamenum = 0;
                        for (Client c : currentGameRoom.getAllClients()) {
                            if (c.isPlaying()) playeringamenum++;
                        }
                        if (playeringamenum == 0)
                        {
                            if(timeStop!=null )
                                timeStop.stopThisThread();
                        }
                    }
                }
                //TODO:待完成的玩家服务线程

                }
            }
        }
        catch (IOException ioe)
        {
            /**
             * 在信息传输过程中如果出现危害服务器的问题，则服务器会断开与客户端的连接
             * 将该客户的状态置为最初始的状态
             */
            ioe.printStackTrace();
            stopThisClient(sendStream,getStream);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * 退出当前所处房间
     * @return
     */
    public void leaveRoom() throws IOException {
        System.out.println(currentGameRoom==null);
        List<Client> roomClientList=currentGameRoom.getAllClients();
        System.out.println("循环房间玩家链表大小为"+roomClientList.size());
        //如果退出的玩家不是房主
        if (!currentGameRoom.getMaster().equals(client))
        {
            currentGameRoom.removeClient(client.getId());//当前所在房间移除当前玩家
            List<Client> allClientsIn = currentGameRoom.getAllClients();
            //向房间所有玩家发该玩家退出信息
            for (Client c : allClientsIn)
            {
                if (c.getRoomID().equals(currentGameRoom.getId()))
                {
                    PrintStream printStream = StartServer.clientPrintStreamMap.get(c);
                    printStream.println(Sign.ClientLeaveRoom + client.getId() + Sign.SplitSign + client.getRoomID());//发送玩家退出房间指令加退出玩家的id
                }
            }
        }
        //如果是房主
        else {
            //清除所有房间内玩家并且T出房间
            try{
                //遍历全部在线玩家
                PrintStream sendStream=null;
                Gson gson=new Gson();
                for(Client c: StartServer.onlineClients)
                {
                    //如果玩家属于该房间
                    if(roomClientList.contains(c))
                    {
                        System.out.println("给房间内"+c.getId()+"发送房间关闭消息");
                        //告知房间里面其他人房间已经被删除
                        sendStream = StartServer.clientPrintStreamMap.get(c);
                        String allclientsStr = gson.toJson(StartServer.onlineClients);
                        String roomStr = gson.toJson(StartServer.allGameRoom);
                        if(client.isPlaying())
                        {
                            sendStream.println(Sign.GameOver+allclientsStr+Sign.SplitSign+roomStr);
                            Thread.sleep(1000);
                        }

                        sendStream.println(Sign.RoomDismiss);
                        //删除该房间内该玩家
                        roomClientList.remove(c);
                    }
                    //如果不属于该房间
                    else
                    {
                        System.out.println("给不是房间内的玩家"+c.getId()+"发送房间删除消息");
                        //告知其他不在此房间中的其他在线用户房间删除的信息
                        sendStream= StartServer.clientPrintStreamMap.get(c);
                        sendStream.println(Sign.DeleteRoom +currentGameRoom.getId());
                    }
                }
                GuiShowMes.append("服务器消息：房间："+client.getId()+" 被注销（房主退出）。\n");//gui显示房间注销的消息
            }catch (Exception e){
                e.printStackTrace();
                stopThisClient(sendStream,getStream);
            }
            //清除此房间
            StartServer.allGameRoom.remove(currentGameRoom);
            System.out.println("房主退出房间后房间数目大小"+StartServer.allGameRoom.size());
            //设置当前所在房间为空
            currentGameRoom=null;
        }
    }
    /**
     *
     * @param flag
     */
    public void setisConnected(boolean flag){
        isConnected=flag;
    }

    /**
     * 发送命令函数
     * @param command 发送的命令
     */
    public void sendCommand(String command){
        sendStream.println(command);
        sendStream.flush();
    }

    /**
     * 用于注销当前服务线程服务的玩家账号
     * //TODO:
     */
    public void LogoutPlayer(){

    }

    /**
     *
     * @return 返回发送流
     */
    public PrintStream getSendStream(){
        return sendStream;
    }

    /**
     *
     * @return 收取流
     */
    public BufferedReader getGetStream(){
        return getStream;
    }
    /**
     * 停止当前服务线程实例对象的运行并进行扫尾工作
     *
     * @param sendStream 获取输出流以回复客户端消息和扫尾停止
     * @param getStream 获取输入流进行扫尾停止
     */
    private void stopThisClient(PrintStream sendStream,BufferedReader getStream)
    {
        try {
            //扫尾工作
            if (client != null) {
                StartServer.onlineClients.remove(client);
                StartServer.clientPrintStreamMap.remove(client);
                if (currentGameRoom != null) leaveRoom();
                client.setPlaying(false);
                client.setOline(false);
                //告诉其他在线玩家client离线了
                for (Client c : StartServer.onlineClients) {
                    StartServer.clientPrintStreamMap.get(c).println(Sign.OneClientOffline + client.getId());
                }
            }
            timeStop.stopThisThread();
            sendStream.close();
            getStream.close();
            socket.close();
            isConnected = false;
            this.interrupt();//停止玩家服务线程
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

    }
}
