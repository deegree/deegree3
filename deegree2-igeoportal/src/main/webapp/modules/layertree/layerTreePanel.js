// create namespace
Ext.namespace( 'deegree.igeo' );

deegree.igeo.layerTreeNodeUI = function() {
    deegree.igeo.layerTreeNodeUI.superclass.constructor.apply(this, arguments);
};


Ext.extend(deegree.igeo.layerTreeNodeUI, Ext.tree.TreeNodeUI, {
	
    // modified from Ext.tree.TreeNodeUI
    renderElements : function(n, a, targetNode, bulkRender){
	
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        
        var displayImg = typeof a.img == 'string';
        
        if ( typeof a.infoText == "undefined" ) {
            a.infoText = "";
        }
        
        if ( displayImg ) {
            a.icon = a.img;
        }
        
        var buf = ['<li class="x-tree-node"><div title="' + n.text + '" ext:tree-node-id="',n.id,
            '" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,
            '" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',            
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',
            (a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),
            '" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + 
            		(a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
            a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '>',
            '<span unselectable="on">',n.text,"</span></a>",
            "</div>",'<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>"].join('');

        var nel;
        if ( bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl() ) ) {
            this.wrap = Ext.DomHelper.insertHtml( "beforeBegin", nel, buf );
        }else{
            this.wrap = Ext.DomHelper.insertHtml( "beforeEnd", targetNode, buf );
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        
        if(cb){
            this.checkbox = cs[3];
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;

        this.otherAttributes = a;
    },
    
    getDDHandles : function(){
       return [this.iconNode, this.textNode, this.elNode];
    },

    check : function(state, descend, bulk) {
    	// implement simple check function to avoid event propagation
    	this.checkbox.checked = state;		
    },
    
    toggleCheck : function(state) {
    	// implement simple toggleCheck function to avoid event propagation
		this.check(state, true);
	}

});

deegree.igeo.layerTreePanel = function(config) {
    
    deegree.igeo.layerTreePanel.superclass.constructor.call(this, config);    
    
    this.rootVisible = false;
    this.loader = new Ext.tree.TreeLoader({
                    dataUrl: config.dataUrl,
                    baseParams: {node: null},
                    baseAttrs: { checked: false,
                                 uiProvider: deegree.igeo.layerTreeNodeUI }
                });
        
    var root = new Ext.tree.AsyncTreeNode({
        text: 'dummy',
        iconCls: 'folder',
        draggable:false,
        expanded:true,
        id:'root'
    });
    
    this.setRootNode(root);
    
    this.nodeCallbacks = {}; 

    var self = this; // for use in following closures 

    // append events on new child nodes
    this.on('beforeappend', function(tree, node, child) {
        // don't add events to existing nodes 
        // (append is triggered if node is moved to end of the tree)
        if ( ! child.eventsAppended ) {
            if (typeof self.nodeAppendCallback == "function") {
                self.nodeAppendCallback.apply(self, arguments); 
            }
            child.eventsAppended = true;
        }
    });
    this.nodeIDCounter = 0;
    this.getNextNodeID = function() {
        return "auto-node-id-" + nodeIDCounter++;
    }
}

Ext.extend(deegree.igeo.layerTreePanel, Ext.tree.TreePanel, {
    // update list with all checked nodes
    updateCheckedNodes: function() {
        if (typeof this.store != "undefined") {
            console.log("update_checked_nodes");
            var list = this.getChecked().map(function(node) { 
                var attr = node.getUI().otherAttributes; 
                console.log(attr);
                return [ attr.text, attr.icon, node ];
            });
            this.store.loadData(list);
        }
        // highlight all tree nodes with checked checkbox
        // Ext.select('div.x-tree-node-el:has(:checked)').highlight()
    },
    dispatchToCallbacks: function(type, args) {
        if (typeof this.nodeCallbacks[type] == "function") {
               return this.nodeCallbacks[type].apply( this, args );
        }
    },
    addNode: function( attributes ) {
        var parentNode = this.getSelectionModel().getSelectedNode();
        if ( parentNode == null ) {
            parentNode = this.getRootNode();
        } else if ( parentNode.leaf == true ) {
            parentNode = parentNode.parentNode;
        }
        var newNode = new Ext.tree.TreeNode({
            text: 'neuer Knoten',
            cls: 'folder',
            expanded: true,
            expandable: true,
            id: this.getNextNodeID(),
            leaf: false,
            checked: false
        });
        // put new node on top
        if ( parentNode.childNodes.length == 0 ) {
            parentNode.appendChild( newNode );
        } else {
            parentNode.insertBefore( newNode, parentNode.item(0) );
            // insertBefore doesn't fire beforeappend event
            parentNode.fireEvent( 'beforeappend', this, parentNode, newNode );
        }
        return newNode;
    }
});
