/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.module.feedreader;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sim
 */
@XmlRootElement(name = "FeedSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeedSettings
{

   private String name;
   private String url;
   private String networkkey;
   private String target;
   private Date lastfetch;

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getNetworkkey() {
      return networkkey;
   }

   public void setNetworkkey(String networkkey) {
      this.networkkey = networkkey;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getTarget() {
      return target;
   }

   public void setTarget(String target) {
      this.target = target;
   }

   public Date getLastfetch() {
      return lastfetch;
   }

   public void setLastfetch(Date lastfetch) {
      this.lastfetch = lastfetch;
   }

}
