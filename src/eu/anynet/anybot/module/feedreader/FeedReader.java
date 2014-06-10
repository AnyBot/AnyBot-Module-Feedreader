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
import eu.anynet.anybot.bot.Bot;
import eu.anynet.anybot.bot.IRCMessageArguments;
import eu.anynet.anybot.bot.Module;
import eu.anynet.anybot.pircbotxextensions.MessageEventEx;
import eu.anynet.java.util.Serializer;
import eu.anynet.java.util.TimerTask;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import org.pircbotx.hooks.events.MessageEvent;

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

      this.tsk = new TimerTask(600000, false)
      {
         @Override
         public void doWork()
         {
            if(me.feeds.getFeeds()!=null && me.feeds.getFeedCount()>0)
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
         }
      };
      this.tsk.start();

   }

   @Override
   public void dispose()
   {
      if(this.tsk!=null && this.tsk.isRunning())
      {
         this.tsk.stop();
         this.tsk = null;
      }
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
                  this.getBot().sendIRC().message(target.getTarget(), "[feedreader] No last fetch date found. Print only the first "+max+" entries to avoid spamming.");
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
                     this.getBot().sendIRC().message(target.getTarget(), "["+feedsettings.getName()+"] "+feedentry.getTitle()+" - "+feedentry.getLink());
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
   public void onMessage(MessageEventEx event) throws Exception
   {

      if(event.args().isMatch("^feed (add|list|remove)"))
      {
         if(event.isChannelAdmin())
         {

            if(event.args().get(1).equalsIgnoreCase("add") && event.args().count()>3)
            {
               String url = event.args().get(2);
               String name = event.args().get(3, -1, " ");

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
               newtarget.setNetworkkey(this.getNetworksettings().getKey());
               newtarget.setTarget(event.getResponseTarget());
               newfeed.addTarget(newtarget);

               if(exist==false)
               {
                  this.feeds.addFeed(newfeed);
               }
               this.feeds.serialize();
               event.respond("Feed added!");
            }
            else if(event.args().get(1).equalsIgnoreCase("list"))
            {
               for(FeedSettings feed : this.feeds.getFeeds())
               {
                  event.respond(this.feeds.getFeeds().indexOf(feed)+") "+feed.getUrl()+" ("+feed.getName()+")");
               }
            }
            else if(event.args().get(1).equalsIgnoreCase("remove") && event.args().count()>2 && event.args().isPartNumeric(2))
            {
               int i = event.args().getInt(2);
               if(this.feeds.getFeeds().size()>i)
               {
                  FeedSettings feed = this.feeds.getFeeds().get(i);
                  FeedTarget newtarget = new FeedTarget();
                  newtarget.setNetworkkey(this.getNetworksettings().getKey());
                  newtarget.setTarget(event.getResponseTarget());

                  if(feed.containsTarget(newtarget))
                  {
                     feed.removeTarget(newtarget);
                  }

                  if(feed.getTargetCount()<1)
                  {
                     this.feeds.removeFeed(feed);
                  }

                  this.feeds.serialize();
                  event.respond("Feed removed!");

               }
            }

         }
         else
         {
            event.respond("You're not a channel operator!");
         }
      }
   }

}
