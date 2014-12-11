app.server = function(ctx){
    return {
        endpoints:{
            pages:[
                {
                    title:'Impact',
                    url:'impact',
                    path:'impact.jag',
                    secured:true
                },
                {
                    title:'Impact1',
                    url:'impact1',
                    path:'index.jag',
                    secured:true
                }
            ]
        }
    };
};

app.pageHandlers = function(ctx) {
    return {
        onPageLoad: function() {
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                ctx.res.sendRedirect(ctx.appContext+'/login');
                return false;
            }
            return true;
        }
    };
};

/*way to access style.css
* https://localhost:9443/publisher/extensions/app/greg_impact/css/style.css
* */