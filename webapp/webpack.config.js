module.exports = {
     entry: {
        app: [  
            './src/firebase-auth.js', 
            './src/navbar-login', 
            './src/new-post.js',
            './src/find.js',
            './src/navbar-signup.js',
            './src/detailed-view.js'
        ]
     },
     output: {
         path: './bin',
         filename: 'index.bundle.js'
     }
 };