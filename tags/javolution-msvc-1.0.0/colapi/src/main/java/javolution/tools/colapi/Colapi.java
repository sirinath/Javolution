/*
 * Colapi - Maven plugin to colorize source code,
 * Copyright (c) 2009 - Colapi (http://javolution.org/colapi)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javolution.tools.colapi;

import java.io.BufferedReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * The colapi mojo.
 *
 * @goal colorize
 * @phase site
 */
public class Colapi extends AbstractMojo {

    /**
     * The location of the directory containing the files to process
     * recurcively.
     * @parameter
     *    expression="${colapi.directory}"
     *    default-value="${project.build.directory}/site/apidocs"
     */
    private File directory;
    /**
     * The pathname filter of the files to colorize (regex).
     * The default value filters out all the files whose pathname
     * does not terminate by .html
     * @parameter
     *     default-value=".*\\.html$"
     */
    private String filter;
    /**
     * The files encoding.
     * @parameter
     *    expression="${project.build.sourceEncoding}"
     *    default-value="UTF-8"
     */
    private String encoding;
    /**
     * The keyword color (combined RGB value).
     * @parameter
     *    expression="${colapi.keyword.color}"
     *    default-value="#7F0055"
     */
    private String keywordColor;
    /**
     * The comment color (combined RGB value).
     * @parameter
     *    expression="${colapi.comment.color}"
     *    default-value="#3F7F5F"
     */
    private String commentColor;
    /**
     * The string color (combined RGB value).
     * @parameter
     *    expression="${colapi.comment.string}"
     *    default-value="#0000A0"
     */
    private String stringColor;

    public void execute() throws MojoExecutionException {
        if (!directory.exists()) {
            throw new MojoExecutionException("Directory: " + directory + " does not exist.");
        }
        _pattern = Pattern.compile(filter);
        try {
            if (directory.isDirectory()) {
                processDirectory(directory);
            } else {
                processFile(directory);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not colorize", e);
        } finally {
            getLog().info("Colapi processed " + _processed + " files and modified " + _modified + " in directory " + directory);
        }
    }
    private Pattern _pattern;

    private void processDirectory(File dir) throws Exception {
        File[] files = dir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                if (!pathname.isFile()) {
                    return false; // Directory.
                }
                return _pattern.matcher(pathname.getPath()).matches();
            }
        });
        for (int i = 0; i < files.length; i++) {
            processFile(files[i]);
        }
        File[] dirs = dir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (int i = 0; i < dirs.length; i++) {
            processDirectory(dirs[i]);
        }
    }

    private void processFile(File file) throws Exception {
        _processed++;
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), encoding));
        _doc.setLength(0);
        int start = -1;
        int state = DATA;
        boolean hasBeenModified = false;
        for (int read = in.read(); read != -1; read = in.read()) {

            // Escape substitution.
            if (state != DATA) {
                if (read == '<') {
                    _doc.append("&lt;");
                } else if (read == '>') {
                    _doc.append("&gt;");
                } else if (read == '&') {
                    _doc.append("&amp;");
                } else {
                    _doc.append((char) read);
                }
            } else {
                _doc.append((char) read);
            }

            switch (state) {
                case DATA:
                    if ((read == ']') && match("[code]")) {
                        _doc.setLength(_doc.length() - 6);
                        _doc.append("<code><pre>");
                        hasBeenModified = true;
                        state = CODE;
                    }
                    break;

                case CODE:
                    if (Character.isJavaIdentifierPart((char) read)) {
                        state = IDENTIFIER;
                        start = _doc.length() - 1;
                    } else if (read == '"') {
                        state = STRING_LITERAL;
                        _doc.insert(_doc.length() - 1, "<font color=\"" + stringColor + "\">");
                    } else if ((read == '/') && (_doc.charAt(_doc.length() - 2) == '/')) {
                        state = COMMENT;
                        _doc.insert(_doc.length() - 2, "<font color=\"" + commentColor + "\">");
                    }
                    break;

                case STRING_LITERAL:
                    if ((read == '"') && (_doc.charAt(_doc.length() - 2) != '\\')) {
                        _doc.append("</font>");
                        state = CODE;
                    }
                    break;

                case IDENTIFIER:
                    if ((read == ']') && match("[/code]")) {
                        _doc.setLength(_doc.length() - 7);
                        _doc.append("</pre></code>");
                        state = DATA;
                    } else if ((read == ']') && match("[code]")) {
                        getLog().error("Nested [code] tag found in file: " + file);
                    } else if (!Character.isJavaIdentifierPart((char) read)) { // End of identifier.
                        String name = _doc.substring(start, _doc.length() - 1);
                        if (IDENTIFIERS.contains(name)) { // Identifier found.
                            _doc.insert(start + name.length(), "</b></font>");
                            _doc.insert(start, "<font color=\"" + keywordColor + "\"><b>");
                        }
                        state = CODE;
                    }
                    break;

                case COMMENT:
                    if ((read == '\n') || (read == '\r')) {
                        _doc.insert(_doc.length() - 1, "</font>");
                        state = CODE;
                    }
                    break;
            }
        }
        in.close();

        if (hasBeenModified) {
            _modified++;
            OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(file), encoding);
            out.write(_doc.toString());
            out.close();
            if (state != DATA) {
                getLog().error("Terminating [/code] tag not found in file: " + file);
            }
        }
    }
    private int _processed;
    private int _modified;
    private StringBuffer _doc = new StringBuffer(10000);

    // Matches the end of the document with the specified string.
    private boolean match(String str) {
        int docLength = _doc.length();
        int strLength = str.length();
        if (docLength < strLength) {
            return false;
        }
        for (int i = 0, j = docLength - strLength; i < strLength;) {
            if (_doc.charAt(j++) != str.charAt(i++)) {
                return false;
            }
        }
        return true;
    }
    // Constants.
    private static final int DATA = 0;
    private static final int CODE = 1;
    private static final int IDENTIFIER = 2;
    private static final int COMMENT = 3; // Can only be end of line comments.
    private static final int STRING_LITERAL = 4;
    private static final String[] KEYWORDS = {"abstract", "continue", "for",
        "new", "switch", "assert", "default", "if", "package",
        "synchronized", "boolean", "do", "goto", "private", "this",
        "break", "double", "implements", "protected", "throw", "byte",
        "else", "import", "public", "throws", "case", "enum", "instanceof",
        "return", "transient", "catch", "extends", "int", "short", "try",
        "char", "final", "interface", "static", "void", "class", "finally",
        "long", "strictfp", "volatile", "const", "float", "native",
        "super", "while"};
    private static final HashSet IDENTIFIERS = new HashSet();

    static {
        for (int i = 0; i < KEYWORDS.length; i++) {
            IDENTIFIERS.add(KEYWORDS[i]);
        }
    }
}