package org.example;

import io.specmesh.blackbox.testharness.Library;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    public int doThings() {

        new Library().someLibraryMethod();
        return 1;

    }
}