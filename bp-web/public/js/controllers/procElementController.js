



/**
 * BP Elements
 */
// INDEX
minorityControllers.controller('BPelementListCtrl', ['$window','$filter', '$rootScope', '$scope', '$q', '$http', '$routeParams', 
  'toaster', 
  'BPStationsFactory', 
  'BProcessesFactory', 
  'ngDialog', 
  'BPElemsFactory',
  'BPElemFactory', 
  'BPSessionsFactory',
  'BPStationsFactory',
  'BPSpacesFactory', 
  'BPSpaceFactory', 
  'BPSpaceElemsFactory', 
  'BPSpaceElemFactory', 
  'BPStatesFactory',
  'BPStateFactory',
  'BPSessionStatesFactory', 
  'BPSessionStateFactory',
  'RefsFactory',
  'SwitchesFactory',
'SwitchFactory',
'ReactionsFactory',
'ReactionFactory',

  '$location', '$route',
  function ($window, $filter, $rootScope, $scope, $q,$http, $routeParams, toaster, BPStationsFactory, BProcessesFactory, ngDialog, BPElemsFactory, BPElemFactory, BPSessionsFactory, BPStationsFactory, BPSpacesFactory, BPSpaceFactory, BPSpaceElemsFactory, BPSpaceElemFactory, BPStatesFactory, BPStateFactory, BPSessionStatesFactory, BPSessionStateFactory,RefsFactory, SwitchesFactory,SwitchFactory,ReactionsFactory,ReactionFactory, $location, $route) {
    
    
  $scope.route = jsRoutes.controllers.BusinessProcessController;
  $scope.businessSet = $rootScope.business;



  $scope.business = function () {
     return parseInt($window.localStorage.getItem('business'));
      
  }  

$scope.isManager = function () {
  if ($scope.isManagerVal == undefined && $rootScope.manager != undefined) {
    $scope.isManagerVal = $rootScope.manager;
    return $scope.isManagerVal;
  } else {
    return $window.localStorage.manager == "true";
  }
};

$scope.isManagerVal = $scope.isManager();
$scope.isManager();



  $scope.bpelems = BPElemsFactory.query({ BPid: $route.current.params.BPid });
  $scope.states = BPStatesFactory.query({ BPid: $route.current.params.BPid });
  $scope.switches = SwitchesFactory.query({ BPid: $route.current.params.BPid });
  $scope.spaces =  BPSpacesFactory.query({ BPid: $route.current.params.BPid });
  $scope.spaces.$promise.then(function(sps) {
    _.forEach(sps, function(sp){ sp.newSpaceelem = { desc:  "", bprocess: parseInt($route.current.params.BPid), business: $scope.business(), space_role: "container",  comps: [ { "a_string" : null} ] }
});
  });
  $scope.spaceelems = BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });

  BProcessesFactory.query().$promise.then(function(data) {

    
    BPStationsFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(stations) {

    $scope.bp = _.find(data, function(proc) { return proc.id == $route.current.params.BPid});    
    $scope.business = function () {
       return $scope.bp.business;
    }  

    $scope.hasActiveStation = _.filter(stations, function(st) { return st.paused == true && st.process == $scope.bp.id }).length > 0;

    });
  });

$scope.loadResources = function() {
BPElemsFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data) {
BPSpacesFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data2) {
BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data3) {
$scope.states.$promise.then(function(states) {
$scope.switches.$promise.then(function(switches) {
    

    $scope.bpelems = data;
    $scope.spaces = data2;
    $scope.spaceelems = data3;

    _.forEach($scope.bpelems, function(z) {
    z.states = _.filter(states, function(d){ return d.front_elem_id == z.id;});
    //z.topo_id = _.find(data.topology, function(d){ return d.front_elem_id == z.id;});
    //z.reactions = _.filter(data.reaction_cn, function(sw) { return sw.reaction.element == z.topo_id }); 

    _.forEach(z.states, function(st){ return st.switches = _.filter(switches, function(sw) { return sw.state_ref == st.id }) });
    /*if (z.b_type == "brick") {
      z.spaces = _.filter(data.unitspace, function(s){ return s.brick_front == z.id;});
        _.forEach(z.spaces, function(sp) { 
              sp.spelems = _.filter(data.unitspaceelement, function(spelem){ 
               return spelem.ref_space_owned == sp.id; 
           }) 
        }); 
    }*/

  });
  _.forEach($scope.spaceelems, function(z) {
    //z.topo_id = _.find(data.topology, function(d){ return d.space_elem_id == z.id;});
    //z.reactions = _.filter(data.reaction_cn, function(sw) { return sw.reaction.element == z.topo_id }); 
    z.states = _.filter(states, function(d){ return d.space_elem_id == z.id;});
    _.forEach(z.states, function(st){ return st.switches = _.filter(switches, function(sw) { return sw.state_ref == st.id }) });
  });
  _.forEach($scope.spaces, function(z) {
    z.states = _.filter(states, function(d){ return d.space_id == z.id;});
    _.forEach(z.states, function(st){ return st.switches = _.filter($scope.switches, function(sw) { return sw.state_ref == st.id }) });
  });

  });
  });
  });
  });
  });
};
$scope.loadResources();


  $scope.mySpaceElems = function(space_elems, space_id) {
     var res = _.filter(space_elems, function(elem){ return elem.space_owned == space_id });
     if (res == undefined) {
      return [];
     } else {
      return res;
     }
  };
  $scope.highlightActive = function() {
    return false;
  }
  $scope.copyProcess = function () {
    var data = {};
    $http.post($scope.route.copy($scope.bp.id, $scope.bp.title + " Copy").absoluteURL(document.ssl_enabled), data).success(function(success) {
        $location.path('/bprocesses');
    }).error(function (error) {
      toaster.pop('error', "Operation fail", "Please try something else");
    });
  };

  $scope.testing = function (elem) {
  $scope.updateElem(elem);
  var z = BPElemsFactory.query({ BPid: $route.current.params.BPid });
}
  /** 
   * Modals window
   */

  $scope.editProcess = function () {
    $scope.BPid = $scope.bp.id;
    $scope.process = $scope.bp;
    ngDialog.open({
      template: 'partials/bprocesses/bp-detail.html',
      controller: 'BProcessDetailCtrl',
      scope: $scope
    });


  };
  $scope.modalShare = function () {
    ngDialog.open({
      template: 'partials/share/share.html',
      controller: 'ProcShareCtrl',
      scope: $scope
    });


  };
  $scope.perm = function() {
    $scope.BPid = $scope.bp.id;
    ngDialog.open({
      template: 'partials/perms/perm-list.html',
      controller: 'BPPermListCtrl',
      scope: $scope
    });
  };

  $scope.openNewElem = function () {
    ngDialog.open({ template: 'partials/modals/newFrontElem.html' });
  };

  /**
   * TREE BUILDER 
   */

  $scope.bpelems.$promise.then(function(data) {
    $scope.spaces.$promise.then(function(data2) {
      $scope.spaceelems.$promise.then(function(data3) {
        $scope.builder();

        $scope.frontSpaces = _.object(_.uniq(_.map($scope.spaces, function(v) {
          return [v.brick_front,
                   _.filter($scope.spaces, function(n){
                      return n.brick_front == v.brick_front;
                    })
                  ]})));
        $scope.nestedSpaces = _.object(_.uniq(_.map($scope.spaces, function(v) {
          return [v.brick_nested,
                   _.filter($scope.spaces, function(n){
                      return n.brick_nested == v.brick_nested;
                    })
                  ]})));


  });
  });
  });
  $scope.elroles =
    [
        { id: 1, type: "container", name: "Container role" },
        { id: 2, type: "subbrick", name: "subbrick role" }
    ];
  $scope.trees = undefined;

  $scope.builder = function () {

    var isIsEnd = function (spElems) {
      _.forEach(spElems, function(val) {
         val.nodes = _.filter(spacesCopy, function(space){ return space.brick_front == val.id || space.brick_nested == val.id; });
      });
      _.forEach(spElems, function(tree) {
           _.forEach(tree.nodes, function(space) {
               space.space_elem = _.filter(spaceelemsCopy, function(spelem){ return spelem.space_owned == space.id; });
               isIsEnd(space.space_elem);
          });
      });
    };
     
    var bpelemsCopy = angular.copy($scope.bpelems);
    var spacesCopy = angular.copy($scope.spaces);
    var spaceelemsCopy = angular.copy($scope.spaceelems);
    $scope.trees = _.forEach(bpelemsCopy, function(val) {
         val.nodes = _.sortBy(_.filter(spacesCopy, function(space){ return space.brick_front == val.id || space.brick_nested == val.id; }), function(em){ return em.order; });
    });
    _.forEach($scope.trees, function(tree) {

      var spaceFetch = function () {
        _.forEach(tree.nodes, function(space) {
           space.space_elem = _.sortBy(_.filter(spaceelemsCopy, function(spelem){ return spelem.space_owned == space.id; }), function(em){ return em.order; });
           isIsEnd(space.space_elem);
        });
      };

      spaceFetch();
      spaceFetch();
      spaceFetch();
      spaceFetch();
      spaceFetch();
    });
}

  $scope.options = {
      accept: function(sourceNode, destNodes, destIndex) {
        var data = sourceNode.$modelValue;
        var destType = destNodes.$element.attr('data-type');
        return (data.type == destType); // only accept the same type
      },
      dropped: function(event) {
         
        var sourceNode = event.source.nodeScope;
        var destNodes = event.dest.nodesScope;
        // update changes to server
        /*if (destNodes.isParent(sourceNode)
          && destNodes.$element.attr('data-type') == 'category') { // If it moves in the same group, then only update group
          var group = destNodes.$nodeScope.$modelValue;
          //group.save();
        } else { // save all
          //$scope.saveGroups();
        }*/
      }
    };


  $scope.reloadTree = function (trees) {
    $scope.trees = undefined;
    var z = function (trees) {

    var isIsEnd = function (spElems) {
      _.forEach(spElems, function(val) {
         val.nodes = _.filter(spacesCopy, function(space){ return space.brick_front == val.id || space.brick_nested == val.id; });
      });
      _.forEach(spElems, function(tree) {
           _.forEach(tree.nodes, function(space) {
               space.space_elem = _.filter(spaceelemsCopy, function(spelem){ return spelem.space_owned == space.id; });
               isIsEnd(space.space_elem);
          });
      });
    };
     
    var bpelemsCopy = angular.copy($scope.bpelems);
    var spacesCopy = angular.copy($scope.spaces);
    var spaceelemsCopy = angular.copy($scope.spaceelems);
    $scope.trees = _.forEach(bpelemsCopy, function(val) {
         val.nodes = _.sortBy(_.filter(spacesCopy, function(space){ return space.brick_front == val.id || space.brick_nested == val.id; }), function(em){ return em.order; });
    });
    _.forEach($scope.trees, function(tree) {

      var spaceFetch = function () {
        _.forEach(tree.nodes, function(space) {
           space.space_elem = _.sortBy(_.filter(spaceelemsCopy, function(spelem){ return spelem.space_owned == space.id; }), function(em){ return em.order; });
           isIsEnd(space.space_elem);
        });
      };

      spaceFetch();
      spaceFetch();
      spaceFetch();
      spaceFetch();
      spaceFetch();
    });
  } 
    BPElemsFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data) {
    BPSpacesFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data2) {
    BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data3) {
    $scope.states.$promise.then(function(states) {
    $scope.switches.$promise.then(function(switches) {
    

    $scope.bpelems = data;
    $scope.spaces = data2;
    $scope.spaceelems = data3;

    _.forEach($scope.bpelems, function(z) {
    z.states = _.filter(states, function(d){ return d.front_elem_id == z.id;});
    //z.topo_id = _.find(data.topology, function(d){ return d.front_elem_id == z.id;});
    //z.reactions = _.filter(data.reaction_cn, function(sw) { return sw.reaction.element == z.topo_id }); 

    _.forEach(z.states, function(st){ return st.switches = _.filter(switches, function(sw) { return sw.state_ref == st.id }) });
    /*if (z.b_type == "brick") {
      z.spaces = _.filter(data.unitspace, function(s){ return s.brick_front == z.id;});
        _.forEach(z.spaces, function(sp) { 
              sp.spelems = _.filter(data.unitspaceelement, function(spelem){ 
               return spelem.ref_space_owned == sp.id; 
           }) 
        }); 
    }*/

  });
  _.forEach($scope.spaceelems, function(z) {
    //z.topo_id = _.find(data.topology, function(d){ return d.space_elem_id == z.id;});
    //z.reactions = _.filter(data.reaction_cn, function(sw) { return sw.reaction.element == z.topo_id }); 
    z.states = _.filter(states, function(d){ return d.space_elem_id == z.id;});
    _.forEach(z.states, function(st){ return st.switches = _.filter(switches, function(sw) { return sw.state_ref == st.id }) });
  });



       z($scope.trees);
  });
  });
  });
  });
  });

  

  }

  $scope.editElem = function (bpId) {
    $location.path('/bp-detail/' + bpId + '/edit');
  };
  $scope.showElem = function (bpId) {
    $location.path('/bprocess/' + bpId + '/show');
  };


  // Flush form
  //
  $scope.flushEditForm = function(form, space) {
   space.newSpaceelem = { desc:  "", process: parseInt($route.current.params.BPid), business: $scope.business,  comps: [ { "a_string" : null} ] };
   form = { desc:  "", process: parseInt($route.current.params.BPid), business: $scope.business,  comps: [ { "a_string" : null} ] };
   $scope.cneedit = false;
  }



  /*
   Elements template
  */
  $scope.newSpace = { process: parseInt($route.current.params.BPid), nestingLevel: 1, container:false,subbrick:false }
  $scope.newBpelem = { desc: "", process: parseInt($route.current.params.BPid), business: $scope.business(), comps: [ { "a_string" : null} ] }
  $scope.newSpaceelem = { desc:  "", process: parseInt($route.current.params.BPid), business: $scope.business, space_role: "container",  comps: [ { "a_string" : null} ] }



  $scope.templateElem = function (template_id, elem) {
    // Confirm
    if (template_id == "confirm") {
      $scope.newBpelem.b_type = "block";
      $scope.newBpelem.type_title = "confirm";
      $scope.newBpelem.comps = [ { "a_bool" : false, "b_bool": false} ];
    }
    // Container
    if (template_id == "container") {
      $scope.newBpelem.b_type = "brick";
      $scope.newBpelem.type_title = "container_brick";
      $scope.newBpelem.comps = [ { "a_bool" : false, "b_bool": false} ];
    }
    // ResAct
  };
  $scope.templateSpaceElem = function (template_id, elem) {
   // Confirm
    if (template_id == "confirm") {
      elem.b_type = "block";
      elem.type_title = "confirm";
      elem.comps = [ { "a_bool" : false, "b_bool": false} ];
    }
    // Container
    if (template_id == "container") {
      elem.b_type = "brick";
      elem.type_title = "container_brick";
      elem.comps = [ { "a_bool" : false, "b_bool": false} ];
    }
    // ResAct
  };


  /* Number generator */
  $scope.orderFrontGen = function (elem) {
        if (elem != undefined) {
    if (_.last($scope.bpelems) != undefined && _.last($scope.bpelems).order != undefined) {
      elem.order = (_.max(_.map($scope.bpelems, function(l){return l.order})) + 1);
      return elem.order;
    }
    else {
      elem.order = 1;
      return 1;
    }
  };
  }

  $scope.orderNestedGen = function (elem) {
    if (elem != undefined) {
      var elms = _.filter($scope.spaceelems, function(elms){ return elms.space_owned == elem.space_owned });
      // 
      // 

    if (_.last(elms) != undefined && _.last(elms).order != undefined) {
      elem.order = (_.max(_.map(elms, function(l){return l.order})) + 1);
      return elem.order;
    }
    else {
      elem.order = 1;
      return 1;
    }
  };
  }
  $scope.indexSpaceGen = function (space) {
    if (space != undefined) {
    if (_.last($scope.spaces) != undefined && _.last($scope.spaces).index != undefined) {
      space.index = _.last($scope.spaces).index + 1;
      return space.index;
    }
    else {
      space.index = 1;
      return 1;
    }
  };
  }




  /* Front CUD */
  $scope.moveUpElem = function (obj) {
    $http({
      url: 'bprocess/' + parseInt($route.current.params.BPid) + '/element/' + obj.id + '/up',
      method: "PUT",
      data: {  }
      })
      .then(function(response) {
        // success
         
        $scope.bpelems = BPElemsFactory.query({ BPid: $route.current.params.BPid });
        $scope.reloadTree($scope.trees);
      },
      function(response) { // optional
        // failed
      }
      );
  };
  $scope.moveDownElem = function (obj) {
    $http({

      url: 'bprocess/' + parseInt($route.current.params.BPid) + '/element/' + obj.id + '/down',
      method: "PUT",
      data: {  }
      })
      .then(function(response) {
        // success
         
        $scope.bpelems = BPElemsFactory.query({ BPid: parseInt($route.current.params.BPid) });
        $scope.reloadTree($scope.trees);
      },
      function(response) { // optional
        // failed
      }
      );
  };



  $scope.updateElem = function (obj) {
     

    BPElemFactory.update(obj).$promise.then(function(data) {
      $scope.bpelems = BPElemsFactory.query({ BPid: $route.current.params.BPid });
      $scope.reloadTree($scope.trees);
      });
  }
  $scope.createNewElem = function () {
     


    BPElemsFactory.create($scope.newBpelem).$promise.then(function(data) {
       
       
    /* Element with spaces */
      if ($scope.newBpelem.type_title == "container_brick1") {
        var space = {
            "bprocess": $scope.newBpelem.bprocess,
            "container": true,
            "subbrick": false,
            "brick_front": data.success,
            "nestingLevel": 1
        };
        $scope.indexSpaceGen(space);
    }

      $scope.bpelems = BPElemsFactory.query({ BPid: $route.current.params.BPid });
      $scope.newBpelem = { desc: "", process: parseInt($route.current.params.BPid), business: $scope.business() };
      $scope.trees = undefined;
      $scope.newselected = 0;
      $scope.reloadTree($scope.trees);
    });

  }

_.findDeep = function(items, attrs) {

  function match(value) {
    for (var key in attrs) {
      if(!_.isUndefined(value)) {
        if (attrs[key] !== value[key]) {
          return false;
        }
      }
    }

    return true;
  }

  function traverse(value) {
    var result;

    _.forEach(value, function (val) {
      if (match(val)) {
        result = val;
        return false;
      }

      if (_.isObject(val) || _.isArray(val)) {
        result = traverse(val);
      }

      if (result) {
        return false;
      }
    });

    return result;
  }

  return traverse(items);

}

$scope.nestedIterator = function(tree) {
var nod_ids = [];
var nsd_ids = [];
_.forEach(tree, function(tr1) {
  _.forEach(tr1.nodes, function(tr2) {
               nod_ids.push(tr2);
    _.forEach(tr2.space_elem, function(tr3) {
               nsd_ids.push(tr3)
      _.forEach(tr3.nodes, function(tr4) {
               nod_ids.push(tr4);
        _.forEach(tr4.space_elem, function(tr5) {
               nsd_ids.push(tr5);
          _.forEach(tr5.nodes, function(tr6) {
               nod_ids.push(tr6);
            _.forEach(tr6.space_elem, function(tr7){
               nsd_ids.push(tr7);
});
});
});
});
});
});
});
return [nod_ids, nsd_ids];
}

$scope.nested_travers = function(obj, spaces, space_elements) {
  var deferred = $q.defer();
  var pms = [];
  var space_init;
  var elems_ids = [];
  var sps_ids = [];
  var init_ids;
  var spelems_init_idsLEFT;
  var post_sps_ids;
  var post_spelems_ids;
  var post_sps_ids1;
  var post_spelems_ids1;
  var post_sps_ids2;
  var post_spelems_ids2;
  var post_sps_ids3 ;
  var post_spelems_ids3;
  var post_sps_ids4;
  var post_spelems_ids4;

  // spaces for root object
  spaces_init = _.filter(spaces, function(sp){ return sp.brick_nested == obj.id }); // || sp.id == obj.space_owned });
  init_ids = _.map(spaces_init, function(sp){ return sp.id }); // init ids
  sps_ids = sps_ids.concat(init_ids);

  space_elems_initLEFT = _.filter(space_elements, function(spelem){ return _.contains(init_ids, spelem.space_owned) && spelem.id != obj.id });
  spelems_init_idsLEFT = _.map(space_elems_initLEFT, function(sp){ return sp.id }); // init ids
  elems_ids = elems_ids.concat(spelems_init_idsLEFT);

   
   
   

   
   



  // POST INIT SHIT
  post_sps_ids = _.map(_.filter(spaces, function(n) { return _.contains(spelems_init_idsLEFT, n.brick_nested) }), function(sp){ return sp.id });
  sps_ids = sps_ids.concat(post_sps_ids);

  post_spelems_ids = _.map(_.filter(space_elements, function(n) { return _.contains(post_sps_ids, n.space_owned) }), function(sp){ return sp.id });
  elems_ids = elems_ids.concat(post_spelems_ids);



  post_sps_ids1 = _.map(_.filter(spaces, function(n) { return _.contains(post_spelems_ids, n.brick_nested) }), function(sp){ return sp.id });
  sps_ids = sps_ids.concat(post_sps_ids1);

  post_spelems_ids1 = _.map(_.filter(space_elements, function(n) { return _.contains(post_sps_ids1, n.space_owned) }), function(sp){ return sp.id });
  elems_ids = elems_ids.concat(post_spelems_ids1);



  post_sps_ids2 = _.map(_.filter(spaces, function(n) { return _.contains(post_sps_ids1, n.brick_nested) }), function(sp){ return sp.id });
  sps_ids = sps_ids.concat(post_sps_ids2);

  post_spelems_ids2 = _.map(_.filter(space_elements, function(n) { return _.contains(post_sps_ids2, n.space_owned) }), function(sp){ return sp.id });
  elems_ids = elems_ids.concat(post_spelems_ids2);



  post_sps_ids3 = _.map(_.filter(spaces, function(n) { return _.contains(post_sps_ids2, n.brick_nested) }), function(sp){ return sp.id });
  sps_ids = sps_ids.concat(post_sps_ids3);

  post_spelems_ids3 = _.map(_.filter(space_elements, function(n) { return _.contains(post_sps_ids3, n.space_owned) }), function(sp){ return sp.id });
  elems_ids = elems_ids.concat(post_spelems_ids3);



  post_sps_ids4 = _.map(_.filter(spaces, function(n) { return _.contains(post_sps_ids3, n.brick_nested) }), function(sp){ return sp.id });
  sps_ids = sps_ids.concat(post_sps_ids4);

  post_spelems_ids4 = _.map(_.filter(space_elements, function(n) { return _.contains(post_sps_ids4, n.space_owned) }), function(sp){ return sp.id });
  elems_ids = elems_ids.concat(post_spelems_ids4);

setTimeout(function() {
  deferred.resolve([sps_ids, elems_ids])
}, 2000);

//return [sps_ids, elems_ids];
//return $q.all(pms);
  return deferred.promise;


}

  function filteringNested(obj){
       
      var deferred = $q.defer();
      var pms = [];

      $scope.nested_travers(obj, $scope.spaces, $scope.spaceelems).then(function(IDS) {
        var spsIds = IDS[0];//_.filter($scope.spaces, function(space) {return space.brick_front == obj.id});
        var spElms = IDS[1];//_.filter($scope.spelem, function(elem) {return _.contain(spsIds, elem.space_own)});
         
         
         
         

        angular.forEach(spsIds, function(sid) {
          pms.push(BPSpaceFactory.delete({ id: sid, BPid: $route.current.params.BPid }).$promise);
        });

        angular.forEach(spElms, function(sid) {
          pms.push(BPSpaceElemFactory.delete({ id: sid, BPid: $route.current.params.BPid }).$promise);
        });


      }); //$scope.nestedIterator([_.findDeep($scope.trees, {'id': obj.id})]);


      //var z = _.map(spsIds, function(sid) { BPSpaceFactory.delete({ id: sid.id, BPid: $route.current.params.BPid }) });
      //var zz = _.map(spElms, function(sid) { BPSpaceElemFactory.delete({ id: sid.id, BPid: $route.current.params.BPid })  });
     
    //deferred.resolve(zz);
  return $q.all(pms);

  //return deferred.promise;

  };

  function filteringNestedInNested(obj){
           

      var deferred = $q.defer();
      var pms = [];


      
     $scope.nested_travers(obj, $scope.spaces, $scope.spaceelems).then(function(IDS) { //$scope.nestedIterator([_.findDeep($scope.trees, {'id': obj.id})]);
        var spsIds = IDS[0]; //_.filter($scope.spaces, function(space) {return space.brick_nested == obj.id});
        var spElms = IDS[1]; //_.filter($scope.spelem, function(elem) {return _.contain(spsIds, elem.space_own)});
         
         
         
         

        angular.forEach(spsIds, function(sid) {
          pms.push(BPSpaceFactory.delete({ id: sid, BPid: $route.current.params.BPid }).$promise);
        });

        angular.forEach(spElms, function(sid) {
          pms.push(BPSpaceElemFactory.delete({ id: sid, BPid: $route.current.params.BPid }).$promise);
        });
         $scope.reloadTree($scope.trees);
     });

      //var z = _.map(spsIds, function(sid) { BPSpaceFactory.delete({ id: sid.id, BPid: $route.current.params.BPid }) });
      //var zz = _.map(spElms, function(sid) { BPSpaceElemFactory.delete({ id: sid.id, BPid: $route.current.params.BPid })  });
     
    //deferred.resolve(zz);
  return $q.all(pms);
  //return deferred.promise;

  };
  $scope.deleteElem = function (obj) {
    BPElemFactory.delete({ id: obj.id, BPid: $route.current.params.BPid }).$promise.then(function(data) {
       /* Element with spaces */
      $scope.bpelems = BPElemsFactory.query({ BPid: $route.current.params.BPid });
      if (obj.type_title == "container_brick11") {

        filteringNested(obj).then(function(data) {
             
             
            $scope.spaces =  BPSpacesFactory.query({ BPid: $route.current.params.BPid });
            $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
         });

      };

      $scope.reloadTree($scope.trees);
    });
  };


  /* SPACES CUD */
  $scope.updateSpace = function (obj) {
     
    BPSpaceFactory.update(obj).$promise.then(function(data) {
      $scope.spaces =  BPSpacesFactory.query({ BPid: $route.current.params.BPid });
    });
  }
  $scope.createNewSpace = function () {
    BPSpacesFactory.create($scope.newSpace).$promise.then(function(data) {

      $scope.spaces =  BPSpacesFactory.query({ BPid: $route.current.params.BPid });
    });
  }
  $scope.deleteSpace = function (obj) {
    BPSpaceFactory.delete({ id: obj.id, BPid: $route.current.params.BPid }).$promise.then(function(data) {
      $scope.spaces =  BPSpacesFactory.query({ BPid: $route.current.params.BPid });
    });
  };





  /* Space Elem CUD */
  $scope.moveUpSpaceElem = function (obj) {
    $http({
      url: 'bprocess/' + parseInt($route.current.params.BPid) + '/space_elem/' + obj.id + '/' + obj.space_owned + '/up',
      method: "PUT",
      data: {  }
      })
      .then(function(response) {
        // success
         
        $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
        $scope.reloadTree($scope.trees);
      },
      function(response) { // optional
        // failed
      }
      );
  };
  $scope.moveDownSpaceElem = function (obj) {
    $http({
      url: 'bprocess/' + parseInt($route.current.params.BPid) + '/space_elem/' + obj.id + '/' + obj.space_owned + '/down',
      method: "PUT",
      data: {  }
      })
      .then(function(response) {
        // success
         
        $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
        $scope.reloadTree($scope.trees);
      },
      function(response) { // optional
        // failed
      }
      );
  };

$scope.createSpaceElemFromSpace = function (obj) {
  BPSpaceElemsFactory.create(obj).$promise.then(function(elem_data) {
/*
    if (obj.type_title == "container_brick") {
       
       

      var space = {
          "bprocess": parseInt($route.current.params.BPid),
          "container": true,
          "subbrick": false,
          "brick_nested": elem_data.success,
          "nestingLevel": _.find($scope.spaces, function(sp){ return sp.id == obj.space_owned }).nestingLevel+1
      };
      $scope.indexSpaceGen(space);

      BPSpacesFactory.create(space).$promise.then(function(space_data) {
        BPSpacesFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data) {

              obj.space_own = space_data.success;
              obj.id = elem_data.success;
              BPSpaceElemFactory.update(obj).$promise.then(function(data) {
                $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
                obj = { desc:  "", bprocess: parseInt($route.current.params.BPid), business: $scope.business,  comps: [ { "a_string" : null} ] };
              });
               _.forEach(data, function(sp){ sp.newSpaceelem = { desc:  "", bprocess: parseInt($route.current.params.BPid), business: $scope.business(), space_owned: sp.id, space_role: "container",  comps: [ { "a_string" : null} ] }});


              $scope.spaces = data;
              $scope.nestedSpaces = _.object(_.uniq(_.map($scope.spaces, function(v) {
                return [v.brick_nested,
                         _.filter($scope.spaces, function(n){
                            return n.brick_nested == v.brick_nested;
                          })
                        ]})));


        });
      });
    };
*/
      $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });

  });
              $scope.cneedit = false;
              $scope.reloadTree($scope.trees);
};
$scope.createSpaceElem = function (obj) {
     

    BPSpaceElemsFactory.create(obj).$promise.then(function(elem_data) {
/*
      if (obj.type_title == "container_brick") {
         
         

        var space = {
            "bprocess": parseInt($route.current.params.BPid),
            "container": true,
            "subbrick": false,
            "brick_nested": elem_data.success,
            "nestingLevel": _.find($scope.spaces, function(sp){ return sp.id == obj.space_owned }).nestingLevel+1
        };
        $scope.indexSpaceGen(space);

        BPSpacesFactory.create(space).$promise.then(function(space_data) {
          BPSpacesFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data) {
                var old_sp = obj.space_own;
                var obj_to_update = obj;
                obj_to_update.space_own = space_data.success;
                obj_to_update.id = elem_data.success;
                BPSpaceElemFactory.update(obj_to_update).$promise.then(function(data) {
                  $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
                  obj = { desc:  "", bprocess: parseInt($route.current.params.BPid), business: $scope.business, space_owned: old_sp,  comps: [ { "a_string" : null} ] };
                });



               _.forEach(data, function(sp){ 
            sp.newSpaceelem = { desc:  "", bprocess: parseInt($route.current.params.BPid), business: $scope.business(), space_owned: sp.id, space_role: "container",  comps: [ { "a_string" : null} ] }});


                $scope.spaces = data;
                $scope.nestedSpaces = _.object(_.uniq(_.map($scope.spaces, function(v) {
                  return [v.brick_nested,
                           _.filter($scope.spaces, function(n){
                              return n.brick_nested == v.brick_nested;
                            })
                          ]})));


          });
        });
      }; */
        $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
        $scope.spaces = BPSpacesFactory.query({ BPid: $route.current.params.BPid }).$promise.then(function(data) {
                       _.forEach(data, function(sp){ sp.newSpaceelem = { desc:  "", bprocess: parseInt($route.current.params.BPid), business: $scope.business(), space_owned: sp.id, space_role: "container",  comps: [ { "a_string" : null} ] }});
        });

        
    });
                 $scope.cneedit = false;
                 $scope.reloadTree($scope.trees);
};

$scope.updateSpaceElem = function (obj) {
     
    BPSpaceElemFactory.update(obj).$promise.then(function(data) {
      $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
      $scope.reloadTree($scope.trees);
    });
};
$scope.deleteSpaceElem = function (obj) {
    BPSpaceElemFactory.delete({ id: obj.id, BPid: $route.current.params.BPid }).$promise.then(function(data) {
      if (obj.type_title == "container_brick11") {

        filteringNestedInNested(obj).then(function(data) {
             
             
            $scope.spaces =  BPSpacesFactory.query({ BPid: $route.current.params.BPid });
            $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
         });
      }
      else {
            $scope.spaceelems =  BPSpaceElemsFactory.query({ BPid: $route.current.params.BPid });
            $scope.reloadTree($scope.trees);
      }
    $scope.reloadTree($scope.trees);
    });
    $scope.reloadTree($scope.trees);
};


$scope.type_short = $rootScope.type_short;


$scope.frontSpace = function (elem_id) { // : spaceObj
    var result = _.find($scope.spaces, function(space){ return space.brick_front == elem_id; });
     
    result;
  }
  $scope.BPid = parseInt($route.current.params.BPid);

    
/***
 * State
 
BPStatesFactory
BPStateFactory

BPSessionStatesFactory
BPSessionStateFactory 
**/
    
/***
 * Reflections
 */    


$scope.refs = RefsFactory.query();

$scope.refs.$promise.then(function(data) {
  console.log(data);
});

    
$scope.refElem = function (ref, elem) {
    elem.ref = ref.ref.id;
}

/***
 * Session observer
 **/   
$scope.sessions = BPSessionsFactory.query({ BPid: $scope.BPid });    
$scope.sessions.$promise.then(function(data){
  BPStationsFactory.query({ BPid: $scope.BPid }).$promise.then(function(stations) {
  _.forEach(data, function(d){ return d.stations = _.filter(stations, function(s){ return s.session == d.id }) });
  })
   if ($scope.sessions.length > 0) {
    $scope.changeSession($scope.sessions[0]);
   }
  });    
    

$scope.changeSession = function(session) {
 $scope.session = session;
 $scope.station = _.find(session.stations, function(s) { return s.front == true })  
 $scope.session_bar = 'shown'; 

 if ($scope.station != undefined && $scope.station.inspace == false) {
   var elem = $scope.bpelems[$scope.station.step].id
   var top = $('.elem-'+elem).offset().top
   $('.traverse-marker').css('top', top-35 + 'px')
 } 
 if ($scope.station != undefined && $scope.station.inspace == true) {
  var spelem = $scope.bpelems[$scope.station.step].id
  var top = $('.spelem-24').offset().top

 }
 $scope.states = BPSessionStatesFactory.query({ BPid: $route.current.params.BPid, id: session.id })
 $scope.states.$promise.then(function (states) {

    _.forEach($scope.bpelems, function(z) {
    z.states = _.filter(states, function(d){ return d.front_elem_id == z.id;});
    //z.topo_id = _.find(data.topology, function(d){ return d.front_elem_id == z.id;});
    //z.reactions = _.filter(data.reaction_cn, function(sw) { return sw.reaction.element == z.topo_id }); 

    _.forEach(z.states, function(st){ return st.switches = _.filter($scope.switches, function(sw) { return sw.state_ref == st.id }) });
    /*if (z.b_type == "brick") {
      z.spaces = _.filter(data.unitspace, function(s){ return s.brick_front == z.id;});
        _.forEach(z.spaces, function(sp) { 
              sp.spelems = _.filter(data.unitspaceelement, function(spelem){ 
               return spelem.ref_space_owned == sp.id; 
           }) 
        }); 
    }*/

  });
  _.forEach($scope.spaceelems, function(z) {
    //z.topo_id = _.find(data.topology, function(d){ return d.space_elem_id == z.id;});
    //z.reactions = _.filter(data.reaction_cn, function(sw) { return sw.reaction.element == z.topo_id }); 
    z.states = _.filter(states, function(d){ return d.space_elem_id == z.id;});
    _.forEach(z.states, function(st){ return st.switches = _.filter($scope.switches, function(sw) { return sw.state_ref == st.id }) });
  });
  _.forEach($scope.spaces, function(z) {
    z.states = _.filter(states, function(d){ return d.space_id == z.id;});
    _.forEach(z.states, function(st){ return st.switches = _.filter($scope.switches, function(sw) { return sw.state_ref == st.id }) });
  });


 });
}
$scope.resetSession = function () {
  $scope.session = undefined;
  $scope.station = undefined;
  $scope.loadResources();
  $('.traverse-marker').css('top', '0px')

}


}]);
    



minorityControllers.controller('BPelementCreationCtrl', ['$scope', 'BPElemsFactory','BPElemFactory', '$location', '$route',
   function ($scope, BPElemsFactory, BPElemFactory, $location, $route) {
    $scope.createNewBPElem = function () {
      BPElemsFactory.create($scope.bpelem);
      $location.path('/bprocesses');
    }
}]);


















