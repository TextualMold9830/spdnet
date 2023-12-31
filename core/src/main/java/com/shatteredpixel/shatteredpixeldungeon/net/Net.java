package com.shatteredpixel.shatteredpixeldungeon.net;

import static java.util.Collections.singletonMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shatteredpixel.shatteredpixeldungeon.net.events.Events;
import com.shatteredpixel.shatteredpixeldungeon.net.windows.NetWindow;
import com.shatteredpixel.shatteredpixeldungeon.net.windows.WndServerInfo;
import com.watabou.noosa.Game;

import org.json.JSONObject;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class Net {
    public static String DEFAULT_SCHEME = "http";
    public static String DEFAULT_HOST = "86.127.212.19";
    public static int DEFAULT_PORT = 5800;
    public static String DEFAULT_KEY = "none";

    private Socket socket;
    private Reciever reciever;
    private Sender sender;
    private ObjectMapper mapper;
    private long seed;

    private NetWindow w;

    public Net(String address, String key){
        URI url = URI.create(address);
        Settings.scheme(url.getScheme());
        Settings.address(url.getHost());
        Settings.port(url.getPort());
        Settings.auth_key(key);
        session(Settings.uri(), key);
    }

    public Net(String key){
        Settings.auth_key(key);
        session(Settings.uri(), key);
    }

    public Net(){
        session(Settings.uri(), Settings.auth_key());
    }

    public void reset(){
        session(Settings.uri(), Settings.auth_key());
    }

    public void session(URI url, String key){
        IO.Options options = IO.Options.builder()
                .setAuth(singletonMap("token", key))
                .setForceNew(true)
                .setReconnection(false)
                .build();
        socket = IO.socket(url, options);
        mapper = new ObjectMapper();
        reciever = new Reciever(this, mapper);
        sender = new Sender(this, mapper);
        setupEvents();
    }

    public void setupEvents(){
        Emitter.Listener onConnected = args -> {
            reciever.startAll();
            if(w != null) {
                Game.runOnRenderThread( () -> w.destroy());
            }
        };

        Emitter.Listener onDisconnected = args -> {
            reciever.cancelAll();
        };

        Emitter.Listener onConnectionError = args -> {
            try {
                JSONObject json = (JSONObject)args[0];
                Events.Error e = mapper().readValue(json.toString(), Events.Error.class);
                NetWindow.error(e.message);
            }catch(Exception e){
                e.printStackTrace();
                NetWindow.error("Connection could not be established!");
            }
            reciever.cancelAll();
            disconnect();
        };

        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
        socket.on(Socket.EVENT_CONNECT, onConnected);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnected);
    }

    public void endEvents(){
        socket.off();
    }

    public void connect() {
        socket.connect();
    }
    public void disconnect(){
        reciever.cancelAll();
        socket.disconnect();
    }

    public void toggle(WndServerInfo w) {
        this.w = w;
        if(!socket.connected())
            connect();
        else
            disconnect();
    }

    public void die(){
        if (socket != null) {
            if(socket.connected()) disconnect();
            endEvents();
            socket = null;
        }
        reciever = null;
        sender = null;
    }

    public void seed(long seed) { this.seed = seed; }
    public long seed() { return this.seed; }

    public Boolean connected() { return socket != null && socket.connected(); }
    public Socket socket(){ return this.socket; }
    public ObjectMapper mapper() { return this.mapper;}
    public Sender sender() { return sender; }
    public URI uri(){ return Settings.uri(); }
}
