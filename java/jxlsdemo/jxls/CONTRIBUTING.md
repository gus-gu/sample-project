# Contributing

1. Create an issue.
2. Fork the Github repository.
3. Clone it into your IDE. A JDK 8 must be configured. Don't use a JRE.
4. Create a feature branch.
5. Write a testcase and modify the code.
6. Commit and push the feature branch.
7. The push message gives you a link to execute for creating the pull request.

## Guidelines for creating an issue in JXLS bug tracker

It is very much recommended to provide a working example which demonstrates the issue. Ideally you should raise a PR to jxls repo demonstrating the issue. Providing an example speeds up the identification of the root cause and the resolution of the issue.

If you are unable to provide an example please describe your issue in detail. Provide an excerpt of the code on how you run JXLS transformation and also attach an input Excel template and an output Excel file you get. Also attach an Excel file with the desired output.

Also in the description please mention which JXLS version you used to reproduce the issue and if you use Microsoft Excel (recommended) or another program.

## Import into Eclipse

1. Clone repository.
2. File > Import > Existing Maven projects
3. Choose parent folder of your jxls repo folder.
4. Projects will be imported as nested projects.

## Codestyle

- Java 8 syntax.
- No wildcards in `import` lines.
- Closing { at end of line.
- Use spaces instead of tabs.
- Use @Override annotation.
- Test case class name must end with 'Test'.

## Writing template based testcases

Use **JxlsTester** for writing a template based testcase. The template file must have the name of the test class and must be in the same package
in source folder 'src/test/resources'
(e.g. `/jxls-poi/src/test/resources/org/jxls/templatebasedtests/ConditionalFormattingTest.xlsx`).
The method processTemplate creates the output file in the target folder with _output as part of the name.
Use JxlsTester.getWorkbook() for verifying the output file.

        // Test
        JxlsTester tester = JxlsTester.xlsx(getClass());
        tester.processTemplate(context);
        
        // Verify
        try (TestWorkbook w = tester.getWorkbook()) {

As an alternative you can use createTransformerAndProcessTemplate() instead of processTemplate() if you want to check, edit or exchange
the transformer instance.

If each test method of your testclass use its own template file you can speficy the method name in the xlsx() call as the 2nd arg (example: NestedSumsTest).

Use xls() if you must use the old XLS format. Prefer the XLSX format.
