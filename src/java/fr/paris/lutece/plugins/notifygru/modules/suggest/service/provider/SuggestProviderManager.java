/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.notifygru.modules.suggest.service.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.paris.lutece.plugins.suggest.business.Suggest;
import fr.paris.lutece.plugins.suggest.business.SuggestFilter;
import fr.paris.lutece.plugins.suggest.business.SuggestHome;
import fr.paris.lutece.plugins.suggest.service.SuggestPlugin;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.provider.IProvider;
import fr.paris.lutece.plugins.workflowcore.service.provider.IProviderManager;
import fr.paris.lutece.plugins.workflowcore.service.provider.InfoMarker;
import fr.paris.lutece.plugins.workflowcore.service.provider.ProviderDescription;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.PluginService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Provider manager of the suggest submits: one provider per suggest using the workflow of the task.
 */
@ApplicationScoped
@Named( SuggestProviderManager.ID )
public class SuggestProviderManager implements IProviderManager
{
    public static final String ID = "notifygru-suggest.ProviderService";

    private static final String MESSAGE_PROVIDER_LABEL = "module.notifygru.suggest.provider.label";
    private static final String MESSAGE_MARKER_PREFIX = "module.notifygru.suggest.marker.";

    @Inject
    private IActionService _actionService;

    @Inject
    private IStateService _stateService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ProviderDescription> getAllProviderDescriptions( ITask task )
    {
        int nIdWorkflow = _actionService.findByPrimaryKey( task.getAction( ).getId( ) ).getWorkflow( ).getId( );
        List<ProviderDescription> listDescriptions = new ArrayList<>( );

        for ( Suggest suggest : SuggestHome.getSuggestList( new SuggestFilter( ), PluginService.getPlugin( SuggestPlugin.PLUGIN_NAME ) ) )
        {
            if ( suggest.getIdWorkflow( ) == nIdWorkflow )
            {
                listDescriptions.add( new ProviderDescription( String.valueOf( suggest.getIdSuggest( ) ), getLabel( suggest ) ) );
            }
        }

        return listDescriptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProviderDescription getProviderDescription( String strProviderId )
    {
        Suggest suggest = SuggestHome.findByPrimaryKey( Integer.parseInt( strProviderId ), PluginService.getPlugin( SuggestPlugin.PLUGIN_NAME ) );
        ProviderDescription description = new ProviderDescription( strProviderId, ( suggest != null ) ? getLabel( suggest ) : strProviderId );
        Collection<InfoMarker> collectionMarkers = new ArrayList<>( );

        for ( String strMarker : SuggestProvider.MARKERS )
        {
            InfoMarker marker = new InfoMarker( strMarker );
            marker.setDescription( I18nService.getLocalizedString( MESSAGE_MARKER_PREFIX + strMarker, I18nService.getDefaultLocale( ) ) );
            collectionMarkers.add( marker );
        }

        description.setMarkerDescriptions( collectionMarkers );

        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProvider createProvider( String strProviderId, ResourceHistory resourceHistory, HttpServletRequest request )
    {
        Action action = _actionService.findByPrimaryKey( resourceHistory.getAction( ).getId( ) );
        State stateAfter = ( ( action != null ) && ( action.getStateAfter( ) != null ) ) ? _stateService.findByPrimaryKey( action.getStateAfter( ).getId( ) ) : null;

        return new SuggestProvider( resourceHistory, ( stateAfter != null ) ? stateAfter.getName( ) : null, request );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId( )
    {
        return ID;
    }

    /**
     * Returns the label of the provider of a suggest.
     *
     * @param suggest
     *            the suggest
     * @return the label
     */
    private static String getLabel( Suggest suggest )
    {
        return I18nService.getLocalizedString( MESSAGE_PROVIDER_LABEL, I18nService.getDefaultLocale( ) ) + suggest.getTitle( );
    }
}
