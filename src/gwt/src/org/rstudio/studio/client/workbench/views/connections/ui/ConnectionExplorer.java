/*
 * ConnectionExplorer.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

package org.rstudio.studio.client.workbench.views.connections.ui;

import org.rstudio.core.client.theme.res.ThemeStyles;
import org.rstudio.core.client.widget.SimplePanelWithProgress;
import org.rstudio.core.client.widget.images.ProgressImages;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.SimpleRequestCallback;
import org.rstudio.studio.client.workbench.views.connections.model.Connection;
import org.rstudio.studio.client.workbench.views.connections.model.ConnectionsServerOperations;
import org.rstudio.studio.client.workbench.views.console.events.ConsoleBusyEvent;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ConnectionExplorer extends Composite implements RequiresResize
{
   public ConnectionExplorer()
   {
      RStudioGinjector.INSTANCE.injectMembers(this);
      
      // code/connecti panel
      int codePanelHeight = 125;
      codePanel_ = new ConnectionCodePanel();
      codePanel_.addStyleName(ThemeStyles.INSTANCE.secondaryToolbarPanel());
      codePanel_.getElement().getStyle().setPadding(8, Unit.PX);
      codePanel_.setHeight(codePanelHeight + "px");
      codePanel_.setWidth("100%");
     
      // table browser panel
      tableBrowser_ = new ListBox();
      tableBrowser_.setVisibleItemCount(2);
      tableBrowser_.setSize("100%", "100%");
      
      
      containerPanel_ = new SimplePanelWithProgress(
                                    ProgressImages.createLarge(), 50);
      
      setConnected(false);
      
      initWidget(containerPanel_);
      
      eventBus_.addHandler(ConsoleBusyEvent.TYPE, new ConsoleBusyEvent.Handler()
      {
         @Override
         public void onConsoleBusy(ConsoleBusyEvent event)
         {
            if (!event.isBusy())
            {
               if (waitingForConnection_)
               {
                  waitingForConnection_ = false;
                  showActivePanel();
                  updateTableBrowser();
               }
            }
         }
      });
   }
   
   @Inject
   public void initialize(EventBus eventBus, ConnectionsServerOperations server)
   {
      eventBus_ = eventBus;
      server_ = server;
   }
   
   public void showConnectionProgress()
   {
      waitingForConnection_ = true;
      containerPanel_.showProgress(100); 
   }
   
   public void setConnection(Connection connection, String connectVia)
   {
      connection_ = connection;
      codePanel_.setCode(connection.getConnectCode(), connectVia);
      updateTableBrowser();
   }
   
   public void setConnected(boolean connected)
   {
      activePanel_ = connected ? tableBrowser_ : codePanel_;
      if (!waitingForConnection_)
         showActivePanel();
   }
   
   public String getConnectCode()
   {
      return codePanel_.getCode();
   }
   
   public String getConnectVia()
   {
      return codePanel_.getConnectVia();
   }
   
   public void updateTableBrowser()
   {
      updateTableBrowser("");
   }
   
   public void updateTableBrowser(String hint)
   {   
      if (waitingForConnection_)
         return;
      
      server_.connectionListTables(
                connection_, 
                new SimpleRequestCallback<JsArrayString>() {
         @Override
         public void onResponseReceived(JsArrayString tables)
         {
            clearTableBrowser();
            tableBrowser_.setVisibleItemCount(Math.max(2, tables.length()));
            for (int i = 0; i<tables.length(); i++)
               tableBrowser_.addItem(tables.get(i));
         }    
      });
   }
   
   public void clearTableBrowser()
   {
      tableBrowser_.clear();
   }
  
   @Override
   public void onResize()
   {
      containerPanel_.onResize();
      
   }
   
   private void showActivePanel()
   {
      containerPanel_.setWidget(activePanel_);
      containerPanel_.onResize();
   }
   
   private final ConnectionCodePanel codePanel_;
   private final ListBox tableBrowser_;
  
   private Widget activePanel_;
   
   private final SimplePanelWithProgress containerPanel_;
   
   private boolean waitingForConnection_ = false;
   
   private Connection connection_ = null;
   
   private EventBus eventBus_;
   private ConnectionsServerOperations server_;
   
}
