package com.shatteredpixel.shatteredpixeldungeon.net.ui;

import static com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.net;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.net.Net;
import com.shatteredpixel.shatteredpixeldungeon.net.Settings;
import com.shatteredpixel.shatteredpixeldungeon.net.windows.NetWindow;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;

import java.net.URI;
import java.net.URISyntaxException;

public class NetBtn extends StyledButton {
    public static final int HEIGHT = 24;
    public static final int MIN_WIDTH = 30;

    private ShatteredPixelDungeon instance = ((ShatteredPixelDungeon) ShatteredPixelDungeon.instance);

    public NetBtn() {
        super(Chrome.Type.GREY_BUTTON_TR, "");
        icon(NetIcons.get(NetIcons.GLOBE));
    }

    @Override
    public void update() {
        super.update();
        icon.brightness(net.connected() ? 0.8f : 0.2f );
    }

    @Override
    protected void onClick() {
        super.onClick();
        NetWindow.showServerInfo();
    }

    @Override
    public boolean onLongClick(){
        promptTextInput();
        return true;
    }


    public void promptTextInput(){
        ShatteredPixelDungeon.scene().addToFront( new WndTextInput("Enter host",
                "Enter the adress of the host",
                "http://"+ Net.DEFAULT_HOST+":"+Net.DEFAULT_PORT,
                59,
                false,
                "Set",
                "Cancel"){
            @Override
            public void onSelect(boolean positive, String text) {
                URI url;
                try {
                    url = new URI(text);
                    Settings.scheme(url.getScheme());
                    Settings.address(url.getHost());
                    Settings.port(url.getPort());
                    System.out.println(Settings.address());
                    System.out.println(Settings.port());
                    if (positive) {
                        Settings.address(text);
                        ShatteredPixelDungeon.scene().addToFront( new WndTextInput("Enter key",
                                "Enter the key",
                                "",
                                59,
                                false,
                                "Set",
                                "Cancel"){
                            @Override
                            public void onSelect(boolean positive, String text){
                                    Settings.auth_key(text);
                            }
                        });
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        );

    }

    }





