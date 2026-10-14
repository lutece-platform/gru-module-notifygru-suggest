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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.suggest.business.Suggest;
import fr.paris.lutece.plugins.suggest.business.SuggestHome;
import fr.paris.lutece.plugins.suggest.business.SuggestSubmit;
import fr.paris.lutece.plugins.suggest.business.SuggestSubmitHome;
import fr.paris.lutece.plugins.suggest.business.SuggestUserInfo;
import fr.paris.lutece.plugins.suggest.business.SuggestUserInfoHome;
import fr.paris.lutece.plugins.suggest.service.SuggestPlugin;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.provider.IProvider;
import fr.paris.lutece.plugins.workflowcore.service.provider.InfoMarker;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.url.UrlItem;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Provider of a suggest submit: the author is the customer, reached by the email suggest stored for them.
 */
public class SuggestProvider implements IProvider
{
    static final String MARK_IDEA_TITLE = "idea_title";
    static final String MARK_IDEA_URL = "idea_url";
    static final String MARK_IDEA_STATE = "idea_state";
    static final String MARK_IDEA_DATE = "idea_date";
    static final String MARK_SUGGEST_TITLE = "suggest_title";
    static final String MARK_AUTHOR_FIRST_NAME = "author_first_name";
    static final String MARK_AUTHOR_LAST_NAME = "author_last_name";
    static final String MARK_AUTHOR_DEPARTMENT = "author_department";
    static final String MARK_SITE_URL = "site_url";
    static final List<String> MARKERS = List.of( MARK_IDEA_TITLE, MARK_IDEA_URL, MARK_IDEA_STATE, MARK_IDEA_DATE, MARK_SUGGEST_TITLE,
            MARK_AUTHOR_FIRST_NAME, MARK_AUTHOR_LAST_NAME, MARK_AUTHOR_DEPARTMENT, MARK_SITE_URL );

    private static final String URL_PORTAL = "jsp/site/Portal.jsp";
    private static final String PARAMETER_PAGE = "page";
    private static final String PARAMETER_ID_SUGGEST = "id_suggest";
    private static final String PARAMETER_ID_SUGGEST_SUBMIT = "id_suggest_submit";
    private static final String PARAMETER_ACTION = "action";
    private static final String ACTION_VIEW_SUGGEST_SUBMIT = "view_suggest_submit";
    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private final String _strIdSuggestSubmit;
    private final String _strCustomerEmail;
    private final Collection<InfoMarker> _collectionMarkers = new ArrayList<>( );

    /**
     * Constructor.
     *
     * @param resourceHistory
     *            the resource history of the suggest submit
     * @param strStateAfter
     *            the name of the workflow state the action leads to
     * @param request
     *            the request, null when the action runs without one
     */
    public SuggestProvider( ResourceHistory resourceHistory, String strStateAfter, HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( SuggestPlugin.PLUGIN_NAME );
        SuggestSubmit submit = SuggestSubmitHome.findByPrimaryKey( resourceHistory.getIdResource( ), plugin );
        Suggest suggest = SuggestHome.findByPrimaryKey( submit.getSuggest( ).getIdSuggest( ), plugin );
        SuggestUserInfo author = ( submit.getLuteceUserKey( ) != null ) ? SuggestUserInfoHome.findByKey( submit.getLuteceUserKey( ), plugin ) : null;

        _strIdSuggestSubmit = String.valueOf( submit.getIdSuggestSubmit( ) );
        _strCustomerEmail = ( author != null ) ? StringUtils.defaultIfBlank( author.getBusinessMail( ), author.getHomeMail( ) ) : null;

        String strSiteUrl = AppPathService.getProdUrl( request );
        UrlItem url = new UrlItem( strSiteUrl + URL_PORTAL );
        url.addParameter( PARAMETER_PAGE, SuggestPlugin.PLUGIN_NAME );
        url.addParameter( PARAMETER_ID_SUGGEST, suggest.getIdSuggest( ) );
        url.addParameter( PARAMETER_ID_SUGGEST_SUBMIT, submit.getIdSuggestSubmit( ) );
        url.addParameter( PARAMETER_ACTION, ACTION_VIEW_SUGGEST_SUBMIT );

        addMarker( MARK_IDEA_TITLE, submit.getSuggestSubmitTitle( ) );
        addMarker( MARK_IDEA_URL, url.getUrl( ) );
        addMarker( MARK_IDEA_STATE, strStateAfter );
        addMarker( MARK_IDEA_DATE, ( submit.getDateResponse( ) != null ) ? new SimpleDateFormat( DATE_PATTERN, Locale.FRANCE ).format( submit.getDateResponse( ) ) : null );
        addMarker( MARK_SUGGEST_TITLE, suggest.getTitle( ) );
        addMarker( MARK_AUTHOR_FIRST_NAME, ( author != null ) ? author.getFirstName( ) : null );
        addMarker( MARK_AUTHOR_LAST_NAME, ( author != null ) ? author.getLastName( ) : null );
        addMarker( MARK_AUTHOR_DEPARTMENT, ( author != null ) ? author.getDepartment( ) : null );
        addMarker( MARK_SITE_URL, strSiteUrl );
    }

    /**
     * Adds a marker with its value.
     *
     * @param strMarker
     *            the marker
     * @param strValue
     *            the value, written empty when null
     */
    private void addMarker( String strMarker, String strValue )
    {
        _collectionMarkers.add( new InfoMarker( strMarker, StringUtils.defaultString( strValue ) ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideDemandId( )
    {
        return _strIdSuggestSubmit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideDemandTypeId( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideDemandSubtypeId( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideDemandReference( )
    {
        return _strIdSuggestSubmit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideCustomerConnectionId( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideCustomerId( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideCustomerEmail( )
    {
        return _strCustomerEmail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideCustomerMobilePhone( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideSmsSender( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<InfoMarker> provideMarkerValues( )
    {
        return _collectionMarkers;
    }
}
