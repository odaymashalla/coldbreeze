'use strict';

/* Services */
var minorityAppServices =
  angular.module(
    'minorityApp.services',
    [
      'ngResource'
    ]
  );



minorityAppServices.factory(
  'Message',
  function($resource) {
    return $resource('/bprocesses');
  });


var baseUrl = jsRoutes.controllers.ProfileController.profile().absoluteURL(document.ssl_enabled);
//'http://localhost\\:9000';

minorityAppServices.factory('AuthInterceptor', function ($window, $q) {
    return {
        request: function(config) {
            config.headers = config.headers || {};
            if ($window.sessionStorage.getItem('token')) {
                $http.defaults.headers.common['auth-token'] = $window.sessionStorage.getItem('token');
                config.headers.Authorization = $window.sessionStorage.getItem('token');
                config.headers.Access_Name = 'John';
            }
            return config || $q.when(config);
        },
        response: function(response) {
            if (response.status === 401) {
              console.log("401");
                // TODO: Redirect user to login page.
            }
            return response || $q.when(response);
        }
    };
});
/***********************************************************************************************************************************
******************************************  BProcess *******************************************************************************
************************************************************************************************************************************/

/*
**  Processes FACTORY
*/
minorityAppServices.factory('BProcessesFactory', function ($resource) {
    return $resource(baseUrl + 'bprocesses', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('BProcessFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:id/info', {}, {
        show: { method: 'GET', params: { id: '@id'} },
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} }
    })
});


/*
** BProc Elements FACTORY
*/
/*
[{
  "id": 6,
  "title": "xx",
  "desc": "",
  "business": 2,
  "bprocess": 2,
  "b_type": "block",
  "type_title": "test block",
  "order": 1,
  "comps": [{
    "a_string": "betas",
    "a_int": 0,
    "b_int": 0,
    "a_bool": false,
    "b_bool": false
  }]
}]

*/
minorityAppServices.factory('BPElemsFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/elements', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST', params: {id: '@BPid'}  }
    })
});
minorityAppServices.factory('BPElemFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/element/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id', BPid: '@bprocess'} },
        delete: { method: 'DELETE', params: {id: '@id', BPid: '@bprocess'} }
    })
});

minorityAppServices.factory('BPSpacesFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/spaces', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST', params: {id: '@BPid'} }
    })
});
minorityAppServices.factory('BPSpaceFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/space/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id', BPid: '@bprocess'} },
        delete: { method: 'DELETE', params: {id: '@id', BPid: '@bprocess'} }
    })
});
minorityAppServices.factory('BPSpaceElemsFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/space_elems', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('BPSpaceElemFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/space_elem/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id', BPid: '@bprocess'} },
        delete: { method: 'DELETE', params: {id: '@id', BPid: '@bprocess'} }
    })
});


/*
** BProc stations FACTORY
*/
minorityAppServices.factory('BPServicesFactory', function ($resource) {
    return $resource(baseUrl + 'bprocesses/services', {}, {
        query: { method: 'GET', isArray: true },
    })
});
minorityAppServices.factory('BPStationsFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/stations', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' },
        note: { method: 'POST' }
    })
});
minorityAppServices.factory('BPStationFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/station/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} } 
    })
});

/*
** Employees FACTORY
*/
minorityAppServices.factory('EmployeesFactory', function ($resource) {
    return $resource(baseUrl + 'actors', {}, {
        query: { method: 'GET', isArray: false }
    })
});
/*
** Permissions FACTORY
*/
minorityAppServices.factory('PermissionsFactory', function ($resource) {
    return $resource(baseUrl + 'elemperms', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});

minorityAppServices.factory('ProcPermissionsFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/elemperms', {}, {
        query: { method: 'GET', isArray: false },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('PermissionFactory', function ($resource) {
    return $resource(baseUrl + 'elemperm/:id', {}, {
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} }
    })
});



/* 
** BPInputLogger FACTORY
*/
minorityAppServices.factory('BPInLoggersFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/input_logs', {}, {
        query: { method: 'GET', isArray: true }
    })
});
minorityAppServices.factory('BPInLoggersStationFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/input_logs/:station_id', {}, {
        query: { method: 'GET', isArray: true }
    })
});
/*
** BPLoggers FACTORY
*/
minorityAppServices.factory('BPLogsFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/logs', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('BPLogFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/log/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} }
    })
});
minorityAppServices.factory('BPLogStationFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/logs/:station_id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {station_id: '@station_id'} },
        delete: { method: 'DELETE', params: {station_id: '@station_id'} }
    })
});


/*
** BPRequest
*/

minorityAppServices.factory('BPRequestFactory', function ($resource) {
    return $resource(baseUrl + 'bprocess/:BPid/request/:station_id', {}, {
        //query: { method: 'GET', isArray: true },
        scheme: { method: 'GET'}, //, params: {bprocess_id: '@id'} },
        send: { method: 'POST' }
    })
});


/***********************************************************************************************************************************
****************************************** Resources *******************************************************************************
************************************************************************************************************************************/

/*
** Business
*/
minorityAppServices.factory('BusinessesFactory', function ($resource) {
    return $resource(baseUrl + 'businesses/', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('BusinessFactory', function ($resource) {
    return $resource(baseUrl + 'business/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} }
    })
});


/*
** Clients
*/
minorityAppServices.factory('ClientsFactory', function ($resource) {
    return $resource(baseUrl + 'business/:BID/clients/', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('ClientFactory', function ($resource) {
    return $resource(baseUrl + 'business/:BID/client/:id', {}, {
        show: { method: 'GET' },
        update: { method: 'PUT', params: {id: '@id'} },
        delete: { method: 'DELETE', params: {id: '@id'} }
    })
});


/*
** Observers
*/
minorityAppServices.factory('ObserversFactory', function ($resource) {
    return $resource(baseUrl + 'observers/:process/:station', {}, {
        query: { method: 'GET', params: {id: '@process', station: '@station'}, isArray: true },
        create: { method: 'POST' }
    })
});
minorityAppServices.factory('ObserverFactory', function ($resource) {
    return $resource(baseUrl + 'observe/:observe_id', {}, {
        show: { method: 'GET' },
        delete: { method: 'DELETE', params: {observe_id: '@observe_id'} }
    })
});



// Demonstrate how to register services
// In this case it is a simple value service.
//angular.module('minorityApp.services', []).
//  value('version', '0.1');
