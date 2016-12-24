package com.publiccms.entities.sys;
// Generated 2016-12-15 17:52:43 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * SysAppClientId generated by hbm2java
 */
@Embeddable
public class SysAppClientId implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int siteId;
    private String channel;
    private String uuid;

    public SysAppClientId() {
    }

    public SysAppClientId(int siteId, String channel, String uuid) {
        this.siteId = siteId;
        this.channel = channel;
        this.uuid = uuid;
    }

    @Column(name = "site_id", nullable = false)
    public int getSiteId() {
        return this.siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    @Column(name = "channel", nullable = false, length = 20)
    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Column(name = "uuid", nullable = false, length = 50)
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof SysAppClientId))
            return false;
        SysAppClientId castOther = (SysAppClientId) other;

        return (this.getSiteId() == castOther.getSiteId())
                && ((this.getChannel() == castOther.getChannel()) || (this.getChannel() != null && castOther.getChannel() != null
                        && this.getChannel().equals(castOther.getChannel())))
                && ((this.getUuid() == castOther.getUuid())
                        || (this.getUuid() != null && castOther.getUuid() != null && this.getUuid().equals(castOther.getUuid())));
    }

    public int hashCode() {
        int result = 17;

        result = 37 * result + this.getSiteId();
        result = 37 * result + (getChannel() == null ? 0 : this.getChannel().hashCode());
        result = 37 * result + (getUuid() == null ? 0 : this.getUuid().hashCode());
        return result;
    }

}
