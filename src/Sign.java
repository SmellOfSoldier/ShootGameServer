public interface Sign
{
    String Register="REGISTER_COMMEND";                                //注册
    String RegisterSuccess="REGISTER_SUCCESS_COMMEND";                 //注册成功
    String Login="LOGIN_COMMEND";                                      //登录
    String LoginSuccess="LOGIN_SUCCESS";                               //登陆成功
    String OpenGame="OPEN_GAME";                                       //玩家打开了点击了多人游戏
    String Disconnect="DISCONNECT";                                    //关闭连接请求（玩家关闭多人游戏界面）
    String EnterRoom="ENTER_ROOM";                                     //加入房间
    String CreateRoom="CREATE_ROOM";                                   //创建房间
    String TickFromRoom="TICK_FROM_ROOM";                              //T出房间
    String LeaveRoom="LEAVE_ROOM";                                     //离开房间
    String RoomDismiss="ROOM_DISMISS";                                 //房间被删除（房主退出）
    String StartGame="START_GAME";                                     //开始游戏
    String GameStart="GAME_START";                                     //游戏已开始
    String NewClientEnter="NEW_CLIENT_ENTER";                          //新玩家加入房间
    String NewRoomCreate="NEW_ROOM_CREATE";                            //新房间创建
    String OneClientTicked="ONE_CLIENT_TICKED";                        //玩家被提出房间
    String ClientLeaveRoom="CLIENT_LEAVE_ROOM";                        //玩家离开房间
    String BeenTicked="BEEN_TICKED";                                   //已经被T出的玩家
    String SuccessDisconnected="SUCCESS_DISCONNEXTED";                 //用于返还给客户端成功断开连接的消息
    String FailedDisconnected="FAILED_DISCONNEXTED";                   //用于返还给客户端连接失败服务器目前不可用的消息
    String SuccessConnected="SUCCESS_CONNEXTED";                       //用于返还给客户端成功连接的消息
    String WrongPassword="WRONG_PASSWORD";                             //错误的密码
    String IsNotRegistered="IS_NOT_REGISTERED";                        //还没有注册过
    String IsRegistered="IS_REGISTERED";                               //已经注册过了
    String Logout="LOGOUT";                                            //玩家注销（退出多人联机）
    String LogoutSuccess="LOGOUT_SUCCESS";                             //成功注销
    String SendPrivateMessage="SEND_PRIVATE_MESSAGE_COMMEND";          //给个人发送信息
    String SendPublicMessage="SEND_PUBLIC_MESSAGE_COMMEND";            //给所有人发送信息
    String ClientExit="CLIENT_EXIT_COMMEND";                           //退出客户端
    String SplitSign="SPLIT_SIGN_COMMEND";                             //信息分隔符
    String Pass="PASS_COMMEND";                                        //密码正确
    String UnPass="UN_PASS_COMMEND";                                   //密码错误
    String SendObject="SEND_OBJECT_COMMEND";                           //发送对象
    String ServerExit="SERVER_EXIT_COMMEND";                           //服务器退出
    String RepeatOnline="REPEAT_ONLINE_COMMEND";                       //帐号被重复登录
    String FromServerMessage="FROM_SERVER_MESSAGE_COMMEND";            //来自服务器的消息
    String OneUserIsOnline="ONE_USER_IS_ONLINE";                       //用户上线
    String OneUserOffOnline="ONE_USER_OFF_ONLINE";                     //用户离线
    String YoursInformation="YOURS_INFORMATION";                       //用户的信息
    String RefreshInformation="REFRESH_INFORMATION";                   //刷新消息请求
    String AddRoom="ADD_ROOM";                                         //增添游戏房间
    String DeleteRoom="DELETE_ROOM";                                   //删除游戏房间
    String OneClientOnline ="ONE_CLIENT_ONLINE";                       //增添玩家
    String OneClientOffline ="ONE_CLIENT_OFFLINE";                     //减少玩家
    String OtherClientLeaveRoom="OTHER_CLIENT_LEAVE_ROOM";             //非当前房间的玩家离开房间
    String OtherClientEnterRoom="OTHER_CLIENT_ENTER_ROOM";             //非当前房间的玩家加入房间
    String PermissionEnterRoom="PERMISSION_ENTER_ROOM";                //允许玩家进入
    String PermissionCreateRoom ="PERMISSION_CREATE_ROOM";             //允许创建房间
    String RoomFull="ROOM_FULL";                                       //房间已满
    String MineBoom="MINE_ROOM";                                       //地雷爆炸
    String GrenadeBoom="GRENADE_BOOM";                                 //手雷爆炸
    String OnePlayerDie="ONE_PLAYER_DIE";                              //一个玩家死亡
    String OnePlayerRelive="ONE_PLAYER_RELIVE";                        //一个玩家复活
    String GameOver="GAME_OVER";                                       //游戏结束
    String PlayerMove="PLAYER_MOVE";                                   //玩家移动
    String PlayerStopMove="PLAYER_STOP_MOVE";                          //玩家停止移动
    String RefreshGameHall="REFRESH_GAME_HALL";                        //刷新游戏大厅
    String CloseLocalThread="CLOSE_LOCAL_THREAD";                      //关闭本地的业务线程
    String GameReadyStart="GameReadyStart";                            //游戏即将开始
    String CreateBullet="CREATE_BULLET";                               //子弹创建
    String CreateMine="CREATE_MINE";                                   //创建地雷
    String CreateGrenade="CREATE_GRENADE";                             //创建手雷
    String RepeatLogin="REPEAT_LOGIN";                                 //多次登陆
    String LeaveGame="LEAVE_GAME";                                     //玩家游戏中离开房间
    String RandomReward="RAND_REWARD";                                 //随机奖励
    String AddHealthPoint="ADD_HEALTH_POINT";                          //增加血量
}
