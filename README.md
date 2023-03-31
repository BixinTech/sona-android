# sona-android
SONA语音房间通用解决方案的Android端项目，包含房间管理、实时音视频、房间IM等功能。

### demo下载
![img.png](doc/img.png)

### 快速接入
 1. 初始化Sona
    ```
    Sona.init(this)
    Sona.openLog()
    initMercury()
    ```
    
    ```
    private fun initMercury() {
        NetworkReceiver.register(this)
        Config.closeMercury = false
        Config.setIp(cn.bixin.sona.demo.constant.Config.MERCURY_IP, cn.bixin.sona.demo.constant.Config.MERCURY_PORT)
        Config.handShakeBody = JSONObject().apply {
            put("d", UUID.randomUUID().toString())
            put("p", 2)
            put("a", 30)
            put("sv", Build.VERSION.SDK_INT.toString())
            put("av", "4.4.2")
            put("u", "80872014")

            val cpuInfo = Misc.cpuInfo()
            val memInfo = Misc.memInfo()
            put("m", Build.MANUFACTURER + " " + Build.MODEL)
            put("e", JSONObject().apply {
                put("cpu", cpuInfo["Hardware"])
                put("memory", memInfo.toString() + "MB")
            })
        }

        Common.exceptionCallback = { type, exceptions ->
            exceptions.forEach {
                it.third.printStackTrace()
            }
            Log.e(TAG, type.toString() + ": " + exceptions.size.toString())
        }

        Common.logCallback = { type, log ->
            Log.e(TAG, "$type: $log")
        }
    }
    ```
 2. 创建SonaRoom
    ```
    val sonaRoom = SonaRoom()
    // 监听房间状态
    sonaRoom.observe { roomEvent, roomEntity ->
       when (roomEvent) {
           RoomEvent.USER_ENTER -> {
               Log.i(TAG, "initSonaRoom: USER_ENTER = ${roomEntity.uid}")
           }
           RoomEvent.USER_LEAVE -> {
               Log.i(TAG, "initSonaRoom: USER_LEAVE = ${roomEntity.uid}")
           }
           RoomEvent.ROOM_CLOSE -> {
               Log.i(TAG, "initSonaRoom: ROOM_CLOSE")
           }
       }
    }
    ```
 3. 注册插件
    ```
    // 按需添加插件
    sonaRoom.addPlugin(AudioPlugin::class.java).config(config)
       .observe(object : AudioPluginObserver {
        
        override fun onDisconnect() { 
        }
        ...
    })
    sonaRoom.addPlugin(...)
    sonaRoom.addPlugin(...)
    ```
 4. 进入房间
    ```
    sonaRoom.enterRoom(roomId, SonaRoomProduct.CHATROOM, "", null, object : SonaRoomCallback {
      override fun onSuccess(roomId: String?) {
        // 进房成功
      }

      override fun onFailed(code: Int, reason: String?) {
        // 进房失败
      }
    })
    ```
 5. 使用插件
    ```
    sonaRoom.getPlugin(AudioPlugin::class.java)?.startSpeak(object : PluginCallback {
      override fun onSuccess() {
        // 推流成功
      }
      override fun onFailure(code: Int, reason: String?) {
        // 推流失败
      }
    })
    ...
    ```
 6. 离开房间
    ```
    sonaRoom.leaveRoom(object : SonaRoomCallback {
      override fun onSuccess(roomId: String?) {
        // 离开房间成功
      }
      override fun onFailed(code: Int, reason: String?) {
        // 离开房间失败
      }
    })
    ```