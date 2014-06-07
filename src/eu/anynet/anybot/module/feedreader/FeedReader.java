/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.module.feedreader;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import eu.anynet.anybot.bot.ChatMessage;
import eu.anynet.anybot.bot.Module;
import eu.anynet.java.util.Serializer;
import eu.anynet.java.util.TimerTask;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author sim
 */
public class FeedReader extends Module
{

   private FeedSettingsStore feeds;
   private TimerTask tsk;

   public FeedReader()
   {

   }

   @Override
   public void launch()
   {
      // Get Settings
      File feedsettingsfile = new File(this.getModuleinfo().getSettingsFolder()+File.separator+"feeds.xml");
      Serializer<FeedSettingsStore> serializer = new FeedSettingsStore().createSerializer(feedsettingsfile);
      if(serializer.isReadyForUnserialize()) {
         feeds = serializer.unserialize();
      } else {
         feeds = new FeedSettingsStore();
      }
      feeds.setModuleInfo(this.getModuleinfo());

      // Launch timer
      final FeedReader me = this;

      this.tsk = new TimerTask(600000)
      {
         @Override
         public void doWork()
         {
            ArrayList<FeedSettings> processed = new ArrayList<>();
            ArrayList<FeedSettings> list;
            synchronized (me)
            {
               list = (ArrayList<FeedSettings>) me.feeds.getFeeds().clone();
            }
            for(FeedSettings feed : list)
            {
               if(me.processFeed(feed))
               {
                  processed.add(feed);
               }
            }
            synchronized (me)
            {
               for(FeedSettings item : processed)
               {
                  int idx = me.feeds.getFeeds().indexOf(item);
                  if(idx>=0)
                  {
                     me.feeds.getFeeds().get(idx).setLastfetch(new Date());
                  }
               }
               me.feeds.serialize();
            }
         }
      };
      this.tsk.start();

   }

   private boolean processFeed(FeedSettings feedsettings)
   {
      boolean result = false;
      try
      {
         SyndFeedInput input = new SyndFeedInput();
         String url = feedsettings.getUrl();
         SyndFeed feed = input.build(new XmlReader(new URL(url)));
         
         int max = -1;
         int i = 0;
         if(feedsettings.getLastfetch()==null)
         {
            max=5;
            for(FeedTarget target : feedsettings.getTargets())
            {
               if(target.getNetworkkey().equals(this.getBot().getNetworkSettings().getKey()))
               {
                  this.getBot().sendMessage(target.getTarget(), "[feedreader] No last fetch date found. Print only the first "+max+" entries to avoid spamming.");
               }
            }
         }

         for(Object entry : feed.getEntries())
         {
            SyndEntry feedentry = (SyndEntry)entry;
            if(feedsettings.getLastfetch()==null || feedsettings.getLastfetch().compareTo(feedentry.getPublishedDate())<0)
            {
               for(FeedTarget target : feedsettings.getTargets())
               {
                  if(target.getNetworkkey().equals(this.getBot().getNetworkSettings().getKey()))
                  {
                     this.getBot().sendMessage(target.getTarget(), "["+feedsettings.getName()+"] "+feedentry.getTitle()+" - "+feedentry.getLink());
                  }
               }
               i++;
               if(max>0 && i>=max)
               {
                  break;
               }
               result = true;
            }
         }
      }
      catch(IOException | IllegalArgumentException | FeedException | java.lang.ExceptionInInitializerError ex)
      {
         this.getBot().sendDebug("[feedreader] "+ex.getClass().getName()+": "+ex.getMessage());
      }
      return result;
   }

   @Override
   public void onMessage(ChatMessage msg)
   {
      if(msg.isBotAsked() && msg.isMatch("^feed add") && msg.count()>3)
      {
         String url = msg.get(3);
         String name = msg.get(4, -1, " ");
         
         boolean exist = false;
         FeedSettings newfeed = new FeedSettings();
         if(this.feeds.getFeedByUrl(url)!=null)
         {
            newfeed = this.feeds.getFeedByUrl(url);
            exist=true;
         }
         
         newfeed.setUrl(url);
         newfeed.setName(name);
         
         FeedTarget newtarget = new FeedTarget();
         newtarget.setNetworkkey(msg.getNetworkSettings().getKey());
         newtarget.setTarget(msg.getResponseTarget());
         newfeed.addTarget(newtarget);
         
         if(exist==false)
         {
            this.feeds.addFeed(newfeed);
         }
         this.feeds.serialize();
         msg.respond("Feed added!");
      }
   }

}
