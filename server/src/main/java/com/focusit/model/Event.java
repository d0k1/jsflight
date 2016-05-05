package com.focusit.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by dkirpichenkov on 04.05.16.
 */
@Document
public class Event {

    public class UiRectangle{
        Float width;
        Float height;

        public Float getWidth() {
            return width;
        }

        public void setWidth(Float width) {
            this.width = width;
        }

        public Float getHeight() {
            return height;
        }

        public void setHeight(Float height) {
            this.height = height;
        }
    }

    public class CSG {
        String a;
        List<String> c;
        String t;
        String i;
        String n;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public List<String> getC() {
            return c;
        }

        public void setC(List<String> c) {
            this.c = c;
        }

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }

        public String getI() {
            return i;
        }

        public void setI(String i) {
            this.i = i;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }
    }

    public class Target1 {
        String getxp;
        CSG csg;
        String gecp;
        String gecs;

        public String getGetxp() {
            return getxp;
        }

        public void setGetxp(String getxp) {
            this.getxp = getxp;
        }

        public CSG getCsg() {
            return csg;
        }

        public void setCsg(CSG csg) {
            this.csg = csg;
        }

        public String getGecp() {
            return gecp;
        }

        public void setGecp(String gecp) {
            this.gecp = gecp;
        }

        public String getGecs() {
            return gecs;
        }

        public void setGecs(String gecs) {
            this.gecs = gecs;
        }
    }

    @Id
    ObjectId id;

    @Indexed
    ObjectId recordingId;

    @Indexed
    String recordName;

    @Indexed
    Long eventId;

    @Indexed
    Long timestamp;

    @Indexed
    String type;

    @Indexed
    String login;

    @Indexed
    String uuid;

    String url;
    String hash;

    @Indexed
    String tabuuid;

    String target;
    List<Target1> target1;
    String target2;

    @Indexed
    String agent;

    @Indexed
    Integer button;
    Integer which;

    Boolean ctrlKey;
    Boolean shiftKey;
    Boolean altKey;
    Boolean metaKey;

    UiRectangle screen;
    UiRectangle window;
    UiRectangle page;
    Float pageX;
    Float pageY;
    Float screenX;
    Float screenY;


    public ObjectId getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = new ObjectId(recordingId);
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Boolean getCtrlKey() {
        return ctrlKey;
    }

    public void setCtrlKey(Boolean ctrlKey) {
        this.ctrlKey = ctrlKey;
    }

    public UiRectangle getScreen() {
        return screen;
    }

    public void setScreen(UiRectangle screen) {
        this.screen = screen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTarget2() {
        return target2;
    }

    public void setTarget2(String target2) {
        this.target2 = target2;
    }

    public List<Target1> getTarget1() {
        return target1;
    }

    public void setTarget1(List<Target1> target1) {
        this.target1 = target1;
    }

    public Integer getButton() {
        return button;
    }

    public void setButton(Integer button) {
        this.button = button;
    }

    public Boolean getShiftKey() {
        return shiftKey;
    }

    public void setShiftKey(Boolean shiftKey) {
        this.shiftKey = shiftKey;
    }

    public Boolean getAltKey() {
        return altKey;
    }

    public void setAltKey(Boolean altKey) {
        this.altKey = altKey;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getWhich() {
        return which;
    }

    public void setWhich(Integer which) {
        this.which = which;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTabuuid() {
        return tabuuid;
    }

    public void setTabuuid(String tabuuid) {
        this.tabuuid = tabuuid;
    }

    public Boolean getMetaKey() {
        return metaKey;
    }

    public void setMetaKey(Boolean metaKey) {
        this.metaKey = metaKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public UiRectangle getWindow() {
        return window;
    }

    public void setWindow(UiRectangle window) {
        this.window = window;
    }

    public UiRectangle getPage() {
        return page;
    }

    public void setPage(UiRectangle page) {
        this.page = page;
    }

    public Float getPageX() {
        return pageX;
    }

    public void setPageX(Float pageX) {
        this.pageX = pageX;
    }

    public Float getPageY() {
        return pageY;
    }

    public void setPageY(Float pageY) {
        this.pageY = pageY;
    }

    public Float getScreenX() {
        return screenX;
    }

    public void setScreenX(Float screenX) {
        this.screenX = screenX;
    }

    public Float getScreenY() {
        return screenY;
    }

    public void setScreenY(Float screenY) {
        this.screenY = screenY;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }
}
