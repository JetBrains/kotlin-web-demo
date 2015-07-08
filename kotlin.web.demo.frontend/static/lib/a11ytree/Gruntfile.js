module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> */\n'
            },
            build: {
                src: 'src/js/<%= pkg.name %>.js',
                dest: 'dist/<%= pkg.name %>.min.js'
            }
        },
        jshint: {
            all: ['src/js/<%= pkg.name %>.js']
        },
        jasmine: {
            src: ['src/js/lib/jquery-1.11.0.js','src/js/jquery.a11yTree.js'],
            options: {
                specs: 'test/js/specs/a11yTreeSpec.js'
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-jasmine');
    grunt.registerTask('default', ['uglify', 'jshint']);
    grunt.registerTask('travis', ['jshint','jasmine']);
};