# Poshi Script

Poshi Script is built off of the original Poshi XML syntax. It has been simplified and streamlined to provide a more readable and writable scripting syntax. For syntax highlighting in a text editor, it works best using syntax highlighting for javascript or groovy files.

The pieces of the syntax itself are divided into two main categories: *[blocks](https://en.wikipedia.org/wiki/Block_(programming))* and *[statements](https://en.wikipedia.org/wiki/Statement_(computer_science))*. Blocks are snippets of code that can contain other blocks or statements, and statements are any kind of variable assignment, or invocation.

## Code Blocks

Code blocks begin with a block name or header followed by a `{` character, and end with a `}` character. Between the braces are the content of the code block. These blocks are used to build the structure of reusable code or test cases, or control the logical flow of the code.

*Examples:*
```javascript
if (isSet(variable)) { // block header
	AssertVisible.assertVisible(); // block content
}

```

### Structural Blocks

Structural blocks are the basic blocks used to create the essential syntax needed to write in Poshi Script. Poshi Script files must minimally contain a `definition` block, which can contain `setUp`, `tearDown`, `test`, or `macro` blocks. For more information, read the descriptions below.


#### `definition` block
All .macro and .testcase files must contain and start with a `definition` block. All other blocks and statements are contained within this `definition` block. Annotations can also be used before definition blocks.

*Examples:*

```javascript
definition {
	...
}
```

**Valid child snippets:** `setup`, `tearDown`, `test`, `property`, and `var` for .testcase files. `macro` and `var` for .macro files.

**Valid parent blocks:** None

---
#### `setUp` and `tearDown` blocks
`setUp` and `tearDown` blocks are used in .testcase files and will run before and after each `test` block, respectively. These can contain common pieces of test code that are run before and/or after each test. These blocks are not required though.

*Examples:*

```javascript
setUp {
	...
}
```

```javascript
tearDown {
	...
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`

**Valid parent blocks:** `definition`

---
#### `test` blocks
In a .testcase file, `test` blocks are used to contain a test case. These are required to create test cases.

*Examples:*

```javascript
test TestCaseName {
	...
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`.

**Valid parent blocks:** `definition`

---
#### `macro` blocks
In a .macro file, `macro` blocks are used to contain a reusable set of test code. These blocks are required to create resuable macro definitions.

*Examples:*

```javascript
macro macroName {
	...
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`.

**Valid parent blocks:** `definition`

---
### Control Flow Blocks

#### `if`, `else if`, and `else` conditional blocks

Conditional statements can be evaluated in `if` and `else if` blocks to determine which set of code to execute upon meeting specific criteria. Current evaluations are if a variable is set, if values of variables are equal, evaluation of a selenium WebDriver boolean method, if a string contains a substring, and other logical operators. `else` blocks do not require a condition, but must have an `if` block preceding it. For valid conditional syntax see the [Conditionals](#conditionals) section below.

*Examples:*
```javascript
if ("${pageStaging}" == "true") {
	Navigator.gotoStagedSitePage(
		pageName = "${pageName}",
		siteName = "${siteName}"
	);
}
else if ("${siteURL}" == "true") {
	Navigator.gotoSitePage(
		pageName = "${pageName}",
		siteName = "${siteName}"
	);
}
else {
	Navigator.gotoPage(pageName = "${pageName}");
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`.

**Valid parent blocks:** All blocks except `definition`.

---

#### `while` loop blocks

`while` blocks function as a while loop and will evaluation the condition iteratively before each execution of the content of the block. For valid conditional syntax see the [Conditionals](#conditionals) section below.

It is also possible to specify a maximum amount of iterations within the while loop. This can be used by passing in a `maxIterations` parameter within the parenthetical content of the block header.

*Examples:*

Basic usage:
```javascript
while (IsElementPresent(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC")) {
	Click.click(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC");
}
```

Usage with `maxIterations` parameter:
```
while (IsElementPresent(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC") && (maxIterations = "16")) {
	Click(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC");
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`.

**Valid parent blocks:** All blocks except `definition`.

---

#### `for` loop blocks
`for` blocks function as a _foreach_ loop, iterating through each item in a collection. Currently, the only valid collections are lists and tables. Note that tables are for use in conjunction with Poshi Prose syntax.

*Examples:*

List example #1
```javascript
var tagNameList = "tag1,tag2";

for (var tagName : list "${tagNameList}") {
	Type.clickAtType(locator1 = "AssetCategorization#CATEGORIES_SEARCH_FIELD", value1 = "${tagName}");

	AssertClick(locator1 = "Button#ADD_TAGS", value1 = "Add");
}
```

List example #2
```javascript
for (var panel : list "Source,Filter,Custom User Attributes,Ordering and Grouping") {
	AssertElementPresent(locator1 = "Panel#PANEL_COLLAPSED", key_panel = "${panel}");
}
```

Raw table example:
```javascript
var RawTable rawTable = new RawTable("${table}");

for (var row : table "${rawTable}") {
	TableEcho.echoTwoVars(
		v0 = "${row[0]}",
		v1 = "${row[1]}"
	);
}
```

Hash table example:
```javascript
var RowsHashTable rowsHashTable = new RowsHashTable("${table}");

for (var row : table "${rowsHashTable}") {
	TableEcho.echoTwoVars(
		v0 = "${row.hash('project_id')}",
		v1 = "${row.hash('status')}"
	);
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`.

**Valid parent blocks:** All blocks except `definition`.

---
### Utility Blocks
#### `task` blocks

`task` blocks can be used to group snippets together and provide a description of that group of snippets. The output is displayed in the console log as well as in the Poshi summary log. Functionally, it makes no impact.

*Examples:*
```javascript
task ("Add a blogs entry called 'Blogs Entry1 Title' with content 'Blogs Entry1 Content'") {
	Navigator.openURL();

	ProductMenu.gotoPortlet(
		category = "Content",
		panel = "Site Administration",
		portlet = "Blogs"
	);

	Blogs.addEntry(
		entryContent = "Blogs Entry1 Content",
		entryTitle = "Blogs Entry1 Title"
	);
}
```

**Valid child snippets:** All statements. All blocks except `definition`, `setUp`, `tearDown`, `test`, and `macro`.

**Valid parent blocks:** All blocks except `definition`.

---
## Code Statements
A code statement is used to describe any kind of reference to various code invocations or variable assignments. Invocations refer to any valid macro, function, Java method or other utility function invocation. Assignments can be any var or property variable assignment.

### Invocations
Invocations can invoke any macro, function, Java method, or predetermined utility function. For specific invocation syntax, see the descriptions for each type of invocation.

For macro and function invocations, the parameter names must be included when passing in parameters. When invoking Java methods, the parameters do not require parameter names but must be wrapped in single quotes.

#### Macro Invocations
Macro invocations execute code that is defined in the macro files. `var` statements can be passed in as parameters in a comma delimited list. Note that the parameter name and value must be stated as and assignment (`name = "value"`), but do not need to be prepended with a `var` keyword.

*Examples:*
```javascript
ProductMenu.gotoPortlet(
	category = "Content",
	panel = "Site Administration",
	portlet = "Blogs"
);
```

---
#### Function Invocations
Function invocations execute code that is defined in the function files. Functions have required parameters that are inherent in their definition and can be: _locator1_, _value1_, _locator2_, and/or _value2_.

Additional variable parameters can also be set for function invocations. These parameters can be added to the comma delimited list of parameters. Note that the variable parameter name and value must be stated as an assignment (`name = "value"`), but do not need to be prepended with a `var` keyword.

Each function file has multiple function commands that can be invoked. There is a "default" function specified in each file, and if no specific function is listed, the "default" function will be invoked. See below for the distinction in the invocation.

*Examples:*

Invoking a specific function of a function file:
```javascript
AssertClick.assertClick(locator1 = "Button#ADD_TAGS", value1 = "Add");
```

Invoking the "default" function of a function file:
```javascript
AssertClick(locator1 = "Button#ADD_TAGS", value1 = "Add");
```

Invoking a function and passing in an additional`var` parameter:
```javascript
Type.sendKeys(
	locator1 = "AlloyEditor#EDITOR",
	value1 = "${kbArticleTitle}",
	key_editor = "title"
);
```

---
#### Java Method Invocations
Certain Java classes and their methods can be invoked using Poshi Script. The valid classes that can be invoked are currently limited to the classes available in the [com.liferay.poshi.runner.util](https://github.com/liferay/com-liferay-poshi-runner/tree/master/poshi-runner/src/main/java/com/liferay/poshi/runner/util) package.

Passing in parameters for these Java method invocations does not require the parameter name, but only the value wrapped in single quotes (same as the syntax used in Poshi XML for "var methods"). Additionally, the full class name or simple class name can be used to invoke methods.

*Examples:*

Java method invocation using the full class name:
```javascript
com.liferay.poshi.runner.util.JSONCurlUtil.post('${curl}');
```

Java method invocation using the simple class name:
```javascript
JSONCurlUtil.post('${curl}');
```

---
#### Utility Invocations
Utility invocations are additional keyword invocations that can invoke simple functionality.

##### `echo` invocations
Using the `echo` invocation will print the specified text in the console. Variables can also be referenced.

*Examples:*
```javascript
echo("Selecting configuration iframe");
```
##### `fail` invocations
Using the `fail` invocation will immediately fail out a test upon execution, as well as print the specified text in the console. Variables can also be referenced.

*Examples:*
```javascript
fail("Please set 'userScreenName'.");
```
##### ~~`takeScreenshot` invocations~~
Currently not working.

*Examples:*
```javascript
takeScreenshot();
```

---
### Assignments
There are two possible types of variable assignments, `var` assignments and `property` assignments. The syntax for assignments is the same, starting with the keyword, followed by the variable name, an equals sign, then the value of the variable. For specific examples, see below.

#### `var` assignments
`var` assignments are how information is stored and referenced within test files. These are generally strings, and can be directly assigned from some invocations. These assignements can also reference objects, but additional development is still necessary to polish this feature. For various ways to assign `var`'s, see the examples below.

##### Basic Strings

*Examples:*
```javascript
var userEmailAddress = "userea@liferay.com";
```

##### Multiline Strings

*Examples:*
```groovy
var wikiPageContent = '''<p id='demo'>PASS</p>

<script type='text/javascript'>
	document.getElementById('demo').innerHTML = 'FAIL';
</script>''';
```

##### Assigning `var`'s to macro invocations

*Examples:*
```javascript
var siteName = TestCase.getSiteName();
```

##### Assigning `var`'s to class/method invocations

*Examples:*
```javascript
var breadcrumbNameUppercase = StringUtil.upperCase('${breadcrumbName}');
```

##### Referencing `var`'s
`var`'s can typically be referenced in any string using the `${}` notation.

*Examples:*
```javascript
var userEmailAddress = "${firstName}.${lastName}@liferay.com";
```

---
#### `property` assignments
`property` assignments are variables that are set in order to be used externally, that is, outside of the the actual test context. These are typically used to help filter tests that get run, as well as denote additional logic that must be run outside of the test context before or after a test runs.

The property variable names are typically separated by `.`'s for multi word names. Each property must first be listed in a `poshi-runner-ext.properties` file by the [`test.case.available.property.names`](https://github.com/liferay/com-liferay-poshi-runner/blob/6339925/poshi-runner/src/main/resources/poshi-runner.properties#L94) property in order to be used in Poshi.

*Examples:*
```javascript
property portal.release = "true";
```

---
## Conditionals
Conditional statements are only used within the parenthetical section of an [`if`, `else if`](#if-else-if-and-else-conditional-block) or [`while`](#while-loop-blocks) block header. When these conditions evaluate to true, the code within the block will execute.

### `var` is set
This returns `true` when a given `var` of specified name is set in the variable context, that is, a `var` name is assigned to some value in the current variable context.

The syntax for using this condition begins with an `isSet` keyword followed by parenthesis wrapped around the `var` name to be evaluated.

*Examples:*
```javascript
if (isSet(duplicate)) {
	Alert.viewErrorMessage(
		errorMessage = "A configuration with this ID already exists. Please enter a unique ID."
	);
}
```

---

### String equals
This returns true when two strings are equal. This is typically used to check a variable reference against a static string, or against another variable reference.

The syntax for using this condition requires double quotes to denote a string. In order to reference a variable, the double quotes must still be used in conjuction with the variable reference syntax (`${}`), followed by a `==` to denote an equality evalution, then followed by the second string.

Please note that the `!=` operator is not currently supported.

*Examples:*
```javascript
if ("${check}" == "true") {
	Alert.viewSuccessMessage();
}
```

---

### String contains
This returns true when one string is contained within another string. This can be used directly with strings or with a reference to `var` that is a string.

The syntax for using this condition begins with a `contains` keyword followed by parenthesis wrapped around two double quoted string parameters. The first parameter is the string, and the second is the substring.

*Examples:*
```javascript
if (contains("testing", "test")) {
	echo("String contains substring");
}
```

---

### Condition `function`
This returns true when a condition `function` is evaluated to true.

The syntax is the same as a normal [function invocation](#function-invocations) without the ending `;`, as it is simply invoking a function that returns a boolean.

*Examples:*
```javascript
while (IsElementPresent(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC")) {
	Click(locator1 = "AssetCategorization#TAGS_REMOVE_ICON_GENERIC");
}
```

---

### Logical Operators
The only [logical operators](https://en.wikipedia.org/wiki/Logical_connective) allowed for conditional syntax are _and_, _or_, and _not_ syntax, which allow the condition to evaluate multiple combinations of conditions and / or their negations.

#### And
This operator can join together two or more conditions and returns true when all of those conditions are also true.

The current syntax requires each separate condition to be wrapped in parenthesis and separated by `&&` between each condition.

*Examples:*
```javascript
if ((IsElementPresent(locator1 = "Blogs#ADD_BLOGS_ENTRY")) && ("${check}" == "true") && (isSet(duplicate))) {
	Alert.viewSuccessMessage();
}

```

---

#### Or
This operator can evaluate two or more conditions and returns true when at least of those conditions is true.

The current syntax requires each separate condition to be wrapped in parenthesis and separated by `||` between each condition.

*Examples:*
```javascript
if ((IsElementPresent(locator1 = "Blogs#ADD_BLOGS_ENTRY")) || ("${check}" == "true") || (isSet(duplicate))) {
	Alert.viewSuccessMessage();
}
```

---

#### Not
This operator returns true when the condition it evaluates is false.

The current syntax requires the condition to be wrapped in parenthesis and prepend by `!`.

Please note that the `!=` operator is not currently supported.

*Examples:*
```javascript
if (!(isSet(duplicate))) {
	Alert.viewErrorMessage(
		errorMessage = "A configuration with this ID already exists. Please enter a unique ID."
	);
}
```

---

## Other
### Comments
[Comments](https://en.wikipedia.org/wiki/Comment_(computer_programming)) can used to add descriptions or notes within the test code. Additionally, comments can be used to wrap existing code so that it is not parsed and executed.

#### Inline comments
To use an inline comment, simply prepending the line with `//` will "comment out" that line.

*Examples:*
```javascript
// This is an inline comment. Only one line is allowed and surrounding white space is not preserved.
```

```javascript
// Multiple inline comments can be used if desired.
// This is equivalent to wrapping text in the multiline syntax.
```

---

#### Multiline comments
To wrap multiple lines of text or code using a multiline comment, prepend the section with `/*` and end the section with `*/`.

*Examples:*
```javascript
/*

This is a multiline comment.
Surrounding white space will be preserved.

*/
```

---
### Annotations
Annotations in Poshi Script are used to store additional meta data for testcases and testcase files. The syntax is similar to an assignment with the annotation variable being prepended by an `@`, followed by an `=`, then followed by a double quoted string of the value.

#### `definition` annotations
These are valid annotations for a [`definition` block](#definition-block).

##### `@component-name`
Every test file requires a `component-name` annotation, and valid component name values must be listed per project in a poshi-runner-ext.properties file by the [`component.name`](https://github.com/liferay/com-liferay-poshi-runner/blob/6339925/poshi-runner/src/main/resources/poshi-runner.properties#L18) property.

*Examples:*
```javascript
@component-name = "portal-acceptance"
definition {
	...
}
```

---

##### `@ignore`
When set to true, this test will not be stored when Poshi files are loaded into the JVM.

*Examples:*
```javascript
@ignore = "true"
definition {
	...
}
```

---

##### `@ignore-command-names`
This can be set to a comma delimited list of testcase command names within the current file, and those tests will not be stored when Poshi files are loaded into the JVM.

*Examples:*
```javascript
@ignore-command-names = "TestCaseCommandName1, TestCaseCommandName2"
definition {
	...
}
```

---

#### `test` annotations
These are valid annotations for [`test` blocks](#test-blocks).

##### `@description`
This is used to describe the usecase of the test.

*Examples:*
```javascript
@description = "Ensure that the super admin can add pages, add portlets, navigate to the product menu, use the WYSIWYG editor, and view alert messages."
test Smoke {
	...
}
```

---

##### `@ignore`
When set to true, this test will not be stored when Poshi files are loaded in the JVM.

*Examples:*
```javascript
@ignore = "true"
test Smoke {
	...
}
```

---

##### `@priority`
This is used to denote the priority of the testcase.

*Examples:*
```javascript
@priority = "5"
test Smoke {
	...
}
```
