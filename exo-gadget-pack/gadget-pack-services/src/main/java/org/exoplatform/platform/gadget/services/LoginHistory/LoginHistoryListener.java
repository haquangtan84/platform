/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.platform.gadget.services.LoginHistory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.Constants;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;


/**
 * Created by The eXo Platform SARL Author : Tung Vu Minh tungvm@exoplatform.com
 * Apr 21, 2011 6:19:21 PM
 */
public class LoginHistoryListener extends Listener<ConversationRegistry, ConversationState> {
	private static final Log LOG = ExoLogger.getLogger(LoginHistoryListener.class);
    private final LoginHistoryService loginHistoryService;

    public LoginHistoryListener(LoginHistoryService loginHistoryService) throws Exception {
        this.loginHistoryService = loginHistoryService;
    }

	/**
	 * Log the time when user logging in 
	 * 
	 * @throws Exception
	 */	
	@Override
	public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
		String userId = event.getData().getIdentity().getUserId();
		try {
			long now = System.currentTimeMillis();
			if(now - loginHistoryService.getLastLogin(userId) > 180000) {
				loginHistoryService.addLoginHistoryEntry(userId, now);
				LOG.info("User " + userId + " logged in.");
			}
			
			/**
			 * JIPT feature sets default pseudo-language to Albanian "sq"
			 */
			OrganizationService svc = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
            // Don't rely on UserProfileLifecycle loaded UserProfile when doing
            // an update to avoid a potential overwrite of other changes
            UserProfile userProfile = svc.getUserProfileHandler().findUserProfileByName(userId);
            if (userProfile != null && userProfile.getUserInfoMap() != null) {
                // Only save if user's locale has not been set
                String currLocale = userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
                if (currLocale == null || currLocale.trim().equals("") || !currLocale.equals("sq")) {
                	//set pseudo-language to Albanian "sq"
                    userProfile.getUserInfoMap().put(Constants.USER_LANGUAGE,"sq");
                    svc.getUserProfileHandler().saveUserProfile(userProfile, false);
                }
            }
			
		} catch (Exception e) {
			LOG.debug("Error while logging the login of user '" + userId + "': " + e.getMessage(), e);
		}
	}
}
