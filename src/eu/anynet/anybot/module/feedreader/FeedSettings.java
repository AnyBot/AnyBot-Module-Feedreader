/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.module.feedreader;

import java.util.ArrayList;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
   private Date lastfetch;

   @XmlElementWrapper(name = "FeedTargets")
   @XmlElement(name = "FeedTarget")
   private ArrayList<FeedTarget> targets = new ArrayList<>();

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Date getLastfetch() {
      return lastfetch;
   }

   public void setLastfetch(Date lastfetch) {
      this.lastfetch = lastfetch;
   }

   public boolean containsTarget(FeedTarget target)
   {
      for(FeedTarget t : this.targets)
      {
         if(target.equals(t))
         {
            return true;
         }
      }
      return false;
   }

   public void addTarget(FeedTarget target)
   {
      if(!this.containsTarget(target))
      {
         this.targets.add(target);
      }
   }

   public FeedTarget getTarget(FeedTarget target)
   {
      for(FeedTarget t : this.getTargets())
      {
         if(t.equals(target))
         {
            return t;
         }
      }
      return null;
   }

   public void removeTarget(FeedTarget target)
   {
      this.targets.remove(this.getTarget(target));
   }

   public ArrayList<FeedTarget> getTargets()
   {
      return this.targets;
   }

   public int getTargetCount()
   {
      return this.targets.size();
   }

}
