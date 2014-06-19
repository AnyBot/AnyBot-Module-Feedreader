/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.module.feedreader;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author perry
 */
@XmlRootElement(name = "FeedTarget")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeedTarget
{

   private String networkkey;
   private String target;

   public FeedTarget(String key, String target)
   {
      this.networkkey = key;
      this.target = target;
   }

   public FeedTarget()
   {
      this(null, null);
   }

   public String getNetworkkey() {
      return networkkey;
   }

   public void setNetworkkey(String networkkey) {
      this.networkkey = networkkey;
   }

   public String getTarget() {
      return target;
   }

   public void setTarget(String target) {
      this.target = target;
   }

   public boolean equals(FeedTarget target)
   {
      return this.target.equals(target.getTarget()) && this.networkkey.contains(target.getNetworkkey());
   }

}
