/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.module.feedreader;

import eu.anynet.anybot.bot.ModuleInfo;
import eu.anynet.java.util.Serializable;
import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author sim
 */
@XmlRootElement(name = "FeedSettingsStore")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeedSettingsStore extends Serializable<FeedSettingsStore>
{

   @XmlTransient
   private ModuleInfo moduleinfo;

   @XmlElementWrapper(name = "Feeds")
   @XmlElement(name = "Feed")
   private ArrayList<FeedSettings> feeds = new ArrayList<>();

   public FeedSettingsStore()
   {
      this.initSerializer(this, FeedSettingsStore.class);
   }

   public void setModuleInfo(ModuleInfo info)
   {
      this.moduleinfo = info;
   }

   @Override
   public String getSerializerFileName() {
      return this.moduleinfo.getSettingsFolder().getAbsolutePath()+File.separator+"feeds.xml";
   }

   public void addFeed(FeedSettings setting)
   {
      this.feeds.add(setting);
   }

   public void removeFeed(FeedSettings setting)
   {
      this.feeds.remove(setting);
   }

   public ArrayList<FeedSettings> getFeeds()
   {
      return this.feeds;
   }

   public FeedSettings getFeedByUrl(String url)
   {
      for(FeedSettings item : this.feeds)
      {
         if(item.getUrl().equals(url))
         {
            return item;
         }
      }
      return null;
   }

   public int getFeedCount()
   {
      return this.feeds.size();
   }


}
