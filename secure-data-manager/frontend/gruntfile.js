/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
const sass = require('node-sass');

const matchdep = require('matchdep'),
    tmp = require('temporary'),
    path = require('path');

const os = require('os');
os.tmpDir = os.tmpdir;

module.exports = function (grunt) {
    'use strict';

    matchdep.filterDev('grunt-*').forEach(grunt.loadNpmTasks);
    const tmpDir = new tmp.Dir();

    grunt.initConfig({

        /* project configuration */

        pkg: grunt.file.readJSON('package.json'),

        prj: {
            src: 'src',
            build: 'build',
            release: 'release',
            desktopApp: 'desktop_app',
            dist: 'dist',
            mockserver: 'server',

            test: 'test',
            fixtures: 'fixtures',
            vendor: 'node_modules',

            jsPattern: '**/*.js',

            files: {
                src: ['<%= prj.js.src %>/<%= prj.jsPattern %>'],
                srcTest: ['<%= prj.test %>/<%= prj.jsPattern %>']
            },

            dependencies: [
                '<%= prj.vendor %>/lodash/lodash.js',
                '<%= prj.vendor %>/angular/angular.js',
                '<%= prj.vendor %>/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js',
                '<%= prj.vendor %>/angular-ui-router/release/angular-ui-router.js',
                '<%= prj.vendor %>/angular-animate/angular-animate.js',
                '<%= prj.vendor %>/angular-aria/angular-aria.js',
                '<%= prj.vendor %>/angular-gettext/dist/angular-gettext.js',
                '<%= prj.vendor %>/angular-material/angular-material.js',
                '<%= prj.vendor %>/angular-promise-extras/angular-promise-extras.js',
                '<%= prj.vendor %>/ng-file-upload/dist/ng-file-upload.js',
                '<%= prj.vendor %>/ng-file-upload/dist/ng-file-upload-shim.js">'

            ],

            cssDependencies: [
                '<%= prj.vendor %>/angular-material/angular-material.css'
            ],

            devDependencies: [
                '<%= prj.vendor %>/angular-mocks/angular-mocks.js'
            ],

            css: {
                src: '<%= prj.src %>/css',
                build: '<%= prj.build %>/css',
                release: '<%= prj.release %>/css'
            },

            sass: {
                src: '<%= prj.src %>/scss',
                build: '<%= prj.css.build %>'
            },

            js: {
                src: '<%= prj.src %>',
                build: '<%= prj.build %>/scripts',
                release: '<%= prj.release %>'
            },

            assets: {
                src: '<%= prj.src %>/assets',
                build: '<%= prj.build %>/assets',
                release: '<%= prj.release %>/assets'
            }
        },

        /* build tasks */

        clean: {
            build: ['<%= prj.build %>'],
            release: ['<%= prj.release %>'],
            dist: ['<%= prj.dist %>'],
            desktopApp: ['<%= prj.desktopApp %>']
        },

        html2js: {
            partials: {
                options: {
                    module: 'partials'
                },
                src: ['<%= prj.src %>/**/*.html'],
                dest: '<%= prj.js.build %>/partials.js'
            }
        },

        sass: {
            options: {
                implementation: sass,
                sourcemap: true
            },
            dist: {
                files: {
                    '<%= prj.sass.build %>/style.css': '<%= prj.sass.src %>/style.scss'
                }
            },
            scss: {
                src: ['<%= prj.sass.src %>/style.scss'],
                dest: '<%= prj.sass.build %>/style.css'
            }

        },


        /* release tasks */

        cssmin: {
            styles: {
                src: [
                    '<%= sass.scss.dest %>',
                    '<%= prj.css.build %>/**/*.css'
                ],
                dest: '<%= prj.css.release %>/<%= pkg.name %>-<%= pkg.version %>.css'
            }
        },

        concat: {
            srcScripts: {
                src: ['<%= prj.js.build %>/**/*.js'],
                dest: '<%= prj.js.release %>/<%= pkg.name %>-<%= pkg.version %>.js'
            },
            scripts: {
                src: [
                    '<%= prj.dependencies %>',
                    '<%= concat.srcScripts.dest %>'
                ],
                dest: '<%= concat.srcScripts.dest %>'
            },
            styles: {
                src: [
                    '<%= prj.cssDependencies %>',
                    '<%= cssmin.styles.dest %>'
                ],
                dest: '<%= cssmin.styles.dest %>'
            }
        },

        ngAnnotate: {
            options: {
                singleQuotes: true
            },
            sources: {
                src: ['<%= concat.srcScripts.dest %>'],
                dest: '<%= concat.srcScripts.dest %>'
            }
        },

        uglify: {
            options: {
                mangle: true
            },
            sources: {
                src: ['<%= concat.srcScripts.dest %>'],
                dest: '<%= concat.srcScripts.dest %>'
            }
        },

        compress: {
            release: {
                options: {
                    archive: '<%= prj.dist %>/<%= pkg.name %>-<%= pkg.version %>.zip'
                },
                expand: true,
                cwd: '<%= prj.desktopApp %>/',
                src: ['**']
            }
        },


        /**
         * Task for gettext and extracting all data into modules
         */
        nggettext_extract: {
            pot: {
                files: {
                    'po/template.pot': [
                        '<%= prj.src %>/**/*.html',
                        '<%= prj.files.src %>'
                    ]
                }
            }
        },

        nggettext_compile: {
            all: {
                options: {
                    format: "json"
                },
                files: [{
                    expand: true,
                    dot: true,
                    cwd: "po",
                    dest: "<%= prj.desktopApp %>/langs",
                    src: ["*.po"],
                    ext: ".json"
                }]
            }
        },

        /* build/release tasks */

        index: {
            build: {
                dir: '<%= prj.build %>',
                src: [
                    '<%= prj.cssDependencies %>',
                    '<%= cssmin.styles.src %>',
                    '<%= prj.dependencies %>',
                    '<%= concat.srcScripts.src %>'
                ]
            },
            release: {
                dir: '<%= prj.release %>',
                src: [
                    '<%= concat.scripts.dest %>',
                    '<%= concat.styles.dest %>'
                ]
            }
        },

        copy: {
            build_css: {
                expand: true,
                cwd: '<%= prj.css.src %>',
                src: ['**/*.css'],
                dest: '<%= prj.css.build %>/'
            },
            build_cssDependencies: {
                expand: true,
                src: ['<%= prj.cssDependencies %>'],
                dest: '<%= prj.build %>/'
            },
            build_scripts: {
                expand: true,
                cwd: '<%= prj.js.src %>',
                src: ['<%= prj.jsPattern %>'],
                dest: '<%= prj.js.build %>/'
            },
            build_dependencies: {
                expand: true,
                src: ['<%= prj.dependencies %>'],
                dest: '<%= prj.build %>/'
            },
            build_assets: {
                expand: true,
                cwd: '<%= prj.assets.src %>/',
                src: ['**'],
                dest: '<%= prj.assets.build %>/'
            },
            release_assets: {
                expand: true,
                cwd: '<%= prj.assets.build %>/',
                src: ['**'],
                dest: '<%= prj.assets.release %>'
            },
            nwjs_package: {
                expand: true,
                cwd: '<%= prj.src %>/',
                src: ['package.json'],
                dest: '<%= prj.build %>'
            },
            nwjs_release_package: {
                expand: true,
                cwd: '<%= prj.src %>/',
                src: ['package.json'],
                dest: '<%= prj.release %>'
            },
            nggettext_langs: {
                expand: true,
                cwd: '<%= prj.desktopApp %>/langs',
                src: ['**'],
                dest: '<%= prj.mockserver %>/responses/langs'
            }

        },

        karma: {
            options: {
                frameworks: ['jasmine'],
                browsers: ['PhantomJS'],
                plugins: [
                    'karma-jasmine',
                    'karma-phantomjs-launcher',
                    'karma-spec-reporter',
                    'karma-junit-reporter',
                    'karma-coverage'
                ],
                preprocessors: {
                    '<%= prj.files.src %>': 'coverage'
                },
                singleRun: true
            },
            build: {
                reporters: [
                    'spec'
                ],
                files: {
                    src: [
                        '<%= prj.dependencies %>',
                        '<%= prj.devDependencies %>',
                        '<%= prj.files.src %>',
                        '<%= prj.files.srcTest %>'
                    ]
                },
                coverageReporter: {
                    type: 'text-summary',
                    dir: tmpDir.path
                }
            },
            release: {
                reporters: [
                    'junit',
                    'coverage'
                ],
                files: '<%= karma.build.files %>',
                junitReporter: {
                    outputFile: '<%= prj.dist %>/test-results.xml'
                },
                coverageReporter: {
                    type: 'lcov',
                    dir: '<%= prj.dist %>/coverage/'
                }
            }
        },

        nwjs: {

            build: {
                options: {
                    version: '0.51.2',
                    buildDir: '<%= prj.desktopApp %>',
                    platforms: ['win64'],
                    mac_icns: '<%= prj.assets.src %>/img/app_icon.icns',
                },
                src: ['<%= prj.build %>/**/*']
            },
            release: {
                options: {
                    buildDir: '<%= prj.desktopApp %>',
                    platforms: ['win64'],
                    mac_icns: '<%= prj.assets.src %>/img/app_icon.icns',
                },
                src: ['<%= prj.release %>/**/*']
            }

        },

        /* dev tasks */

        express: {
            options: {
                port: 8090
            },
            dev: {
                options: {
                    server: path.resolve('./server/mockserver')
                }
            }
        },

        watch: {
            scripts: {
                files: ['<%= prj.files.src %>'],
                tasks: ['karma:build', 'copy:build_scripts']
            },
            assets: {
                files: ['<%= prj.assets.src %>/**'],
                tasks: ['copy:build_assets']
            },
            partials: {
                files: ['<%= html2js.partials.src %>'],
                tasks: ['html2js']
            },
            css: {
                files: ['<%= prj.css.src %>/**/*.css'],
                tasks: ['copy:build_css']
            },
            sass: {
                files: ['<%= prj.sass.src %>/**/*.scss'],
                tasks: ['sass']
            },
            tests: {
                files: ['<%= prj.files.srcTest %>'],
                tasks: ['karma:build']
            }
        }
    });

    grunt.loadTasks('tasks');

    /* main tasks */

    grunt.registerTask('compile', [
        'html2js',
        'sass',
        'copy:build_css',
        'copy:build_scripts',
        'copy:build_dependencies',
        'copy:build_cssDependencies',
        'copy:build_assets',
        'nggettext_compile',
        'copy:nggettext_langs'
    ]);

    grunt.registerTask('build', [
        'clean',
        'compile',
        'index:build'
    ]);

    grunt.registerTask('release', [
        'clean',
        'karma:release',
        'compile',
        'cssmin',
        'copy:release_assets',
        'concat:srcScripts',
        'ngAnnotate',
        'uglify',
        'concat:scripts',
        'concat:styles',
        'index:release',
        'version',
        'copy:nwjs_release_package',
        'nwjs:release',
        'compress:release'
    ]);

    grunt.registerTask('app', [
        'build',
        'copy:nwjs_package',
        'nwjs:build'
    ]);

    grunt.registerTask('default', ['release']);

    /* custom task definitions */

    (function () {

        const ext = function (ext) {
            const extRE = new RegExp('\.' + ext + '$');
            return RegExp.prototype.test.bind(extRE);
        };

        const task = function () {

            const dir = this.data.dir,
                files = this.filesSrc;

            const relativePath = function (file) {
                return file.replace(dir + '/', '');
            };

            const scripts = files.filter(ext('js')).map(relativePath),
                styles = files.filter(ext('css')).map(relativePath),
                dev = grunt.task.current.target === 'build';

            const src = grunt.config('prj.src') + '/index.html',
                dest = dir + '/index.html';

            grunt.file.copy(src, dest, {
                process: function (contents) {
                    return grunt.template.process(contents, {
                        data: {
                            scripts: scripts,
                            styles: styles
                        }
                    });
                }
            });
        };

        grunt.registerMultiTask('index', 'Process index template', task);
    })();

    (function () {
        const task = function () {

            const src = grunt.config('prj.src') + '/version.txt',
                dest = grunt.config('prj.release') + '/version.txt';

            grunt.file.copy(src, dest, {
                process: function (contents) {
                    return grunt.template.process(contents, {
                        data: {
                            pkg: grunt.config('pkg'),
                            env: process.env
                        }
                    });
                }
            });
        };

        grunt.registerTask('version', 'Process version template', task);
    })();
};
