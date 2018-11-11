---
layout: post
title: "Rails Testing Difficulties"
date: 2015-03-23 14:58
comments: true
categories:  rails, testing, ruby
---
I'm trying my best to practice TDD. In my mind, this means "don't write code to solve a problem! Instead, write a test to describe the problem, then write
code to make the test pass." I'm running into a lot of problems making this work, however. Essentially, I like programming because you can do practically
anything if you learn the secret languages (I know that sounds mystical - so be it). I'm beginning to hate TDD because it seems like you can't do much of 
anything.
<!-- more -->
There's a bit of philosophy I fear I am misunderstanding from the TDD camp. As succinctly as possible, it's what I wrote above - if you're writing code to 
solve a problem, then you're doing it wrong. Keeping the code to simply making tests pass keeps the code small and focussed on clearly stated formal goals.
It prevents expository, creative coding. It keeps code from being confusing and seemingly purposeless. Every line of code should be written to address a test
and for no other reason. Of course, rails inserts a lot of code on it's own, but statements like `class Machine1` don't need tests (i.e. a test that insures
that a class called `Machine1` exists in memory is a bad test - there's no need to test ruby itself.) And other code in rails boilerplate is certified to be
"best in class" "best practices" code, and you should trust it. At least that is what they say.

But rails projects don't simply consist of ruby code added to rails boilerplate. When you work on rails projects, you add in gems to perform functions that
you want to "just work" and you need to test the functionality of those gems. Again, you can trust that the gem itself is tested, but you need to test how
you are testing it in your code. And sometimes this just doesn't work, and that is frustrating. Further, since tests run in a different environment than
`development`, and this different environment requires different configuration, it isn't always easy to see why a test is failing. This is also a source of
frustration, since it is relatively easy to test why something is failing in `development`. If it works fine in `development` but it doesn't work in `test`,
WTF?

### What doesn't work: case in point - sidekiq

I use the hell out of [sidekiq](http://sidekiq.org/). I admit I don't understand it, but being about 80% ignorant of the inner workings of the gem, I have been able to use it
for several projects where state machines running independently of web code flow were required. It just works. But it's a pain to test. I have a class called
`PathState` which is basically a node in a graph of a state machine.  It connects to another `PathState` and may or may not contain an executable function
as a string parameter. When a `PathStateRunner` happens upon a particular `PathState`, it may fire a sidekiq worker to perform the `action` parameter and
record the results. This is a critical activity of the project, and thus it should be tested to death. So I wrote a test (it may be a horrible test, but it's
all I can come up with being a newbie to TDD):

``` ruby
  test "should execute action when executed" do
    ps = PathState.create(:name => "TestName", :action => "touch /tmp/PathStateTest")
    ps.execute
    result = `[ -f /tmp/PathStateTest ] && echo 0 || echo 1`
    assert result==0, "OOPS! PathState doesn't execute action when executed!"
  end
```

This test always fails. I wrote code to make the test pass:

#### in the model

``` ruby
  def execute
    PathStateRunner.perform_async(id)
  end
```

#### and the worker

``` ruby
  def perform(path_state_id)
    ps = PathState.find(path_state_id)

    @result = `#{ps.action} 2>&1`
    unless $?.exitstatus.zero?
      # failed state
      Sidekiq.logger.warn "---> PathStateRunner worker failed action: '#{ps.action}'"
    end
  end
```

Again, this always fails. It fails in the same way every time - the worker can't find the `PathState` of that id. When I hop into the console in test and
look for it, I always find it. When I execute the worker from the console giving it an existing `PathState.id`, it always works fine. But you can't run a
test to see it work. I emailed the developer who write sidekiq, who told me you can't test that way, since "The test runner will silently roll back database
transactions so your scenario won't work." I investigate and I can see this in the test log, sure enough:

``` ruby
   (0.4ms)  BEGIN
-------------------------------------------------------
PathStatesControllerTest: test_should_create_path_state
-------------------------------------------------------
  PathState Load (0.4ms)  SELECT  `path_states`.* FROM `path_states` WHERE `path_states`.`id` = 980190962 LIMIT 1
   (0.4ms)  SELECT COUNT(*) FROM `path_states`
Processing by PathStatesController#create as HTML
  Parameters: {"path_state"=>{"action"=>"MyString", "kod_return_value"=>"MyString", "name"=>"MyString", "restart_if_fails"=>false, "success_return_value"=>"MyString"}}
   (0.2ms)  SAVEPOINT active_record_1
  SQL (0.5ms)  INSERT INTO `path_states` (`name`, `action`, `success_return_value`, `kod_return_value`, `restart_if_fails`, `created_at`, `updated_at`) VALUES ('MyString', 'MyString', 'MyString', 'MyString', 0, '2015-03-23 14:48:32.021537', '2015-03-23 14:48:32.021537')
   (0.2ms)  RELEASE SAVEPOINT active_record_1
Redirected to http://test.host/path_states/980191105
Completed 302 Found in 10ms (ActiveRecord: 0.9ms)
   (0.5ms)  SELECT COUNT(*) FROM `path_states`
   (2.2ms)  ROLLBACK
```

`ROLLBACK` happenes every time. So I don't blame sidekiq for not being testable, but I can't imagine that we can accept that "some things are just not
testable" and "test everything" lives in our minds at the same time. Congnative dissonance.

#### another one: 

I another MVC called `CloudformationTemplate` which is a model for holding AWS cf templates for deployment. Creating a `CloudformationTemplate` requires a
reference to an `AwsConfiguration` object in a `belongs_to/has_many` relationship. My test for creating a `CloudformationTemplate` fails every time, and I
can't figure out why. Here's the test:

``` ruby
  test "should create cloudformation_template" do
    assert_difference 'CloudformationTemplate.count' do
      post :create, cloudformation_template: { name: @cloudformation_template.name, aws_configuration: @cloudformation_template.aws_configuration }
    end

    assert_redirected_to cloudformation_template_path(assigns(:cloudformation_template))
  end
```

The test log doesn't show a damn thing useful:

``` ruby
   (0.1ms)  BEGIN
---------------------------------------------------------------------------------
CloudformationTemplatesControllerTest: test_should_create_cloudformation_template
---------------------------------------------------------------------------------
  CloudformationTemplate Load (0.3ms)  SELECT  `cloudformation_templates`.* FROM `cloudformation_templates` WHERE `cloudformation_templates`.`id` = 980190962 LIMIT 1
   (0.3ms)  SELECT COUNT(*) FROM `cloudformation_templates`
  AwsConfiguration Load (0.3ms)  SELECT  `aws_configurations`.* FROM `aws_configurations` WHERE `aws_configurations`.`id` = 982829581 LIMIT 1
Processing by CloudformationTemplatesController#create as HTML
  Parameters: {"cloudformation_template"=>{"name"=>"MyString", "aws_configuration"=>"982829581"}}
Unpermitted parameter: aws_configuration
   (0.2ms)  SAVEPOINT active_record_1
   (0.2ms)  ROLLBACK TO SAVEPOINT active_record_1
  Rendered common/forms/_rounded_buttoned_labelled_text_field.html.erb (0.9ms)
  Rendered common/forms/_rounded_buttoned_labelled_text_field.html.erb (0.6ms)
  Rendered common/forms/_rounded_buttoned_labelled_text_field.html.erb (0.6ms)
  Rendered common/forms/_rounded_buttoned_labelled_text_area.html.erb (4.5ms)
  Rendered common/forms/_rounded_buttoned_labelled_text_area.html.erb (0.6ms)
  AwsConfiguration Load (0.5ms)  SELECT `aws_configurations`.* FROM `aws_configurations`
  Rendered common/forms/_rounded_buttoned_labelled_collection_select.html.erb (3.0ms)
  Rendered cloudformation_templates/_cloudformation_template_fields.html.erb (85.7ms)
  Rendered cloudformation_templates/_form.html.erb (90.8ms)
  Rendered common/_back.html.erb (0.2ms)
  Rendered common/_about_content.html.erb (0.3ms)
  Rendered cloudformation_templates/new.html.erb within layouts/application (94.5ms)
Completed 200 OK in 106ms (Views: 99.0ms | ActiveRecord: 0.9ms)
   (0.3ms)  SELECT COUNT(*) FROM `cloudformation_templates`
   (0.2ms)  ROLLBACK
```

I continue to struggle with this, begging for assistance from smarter developers, re-reading the official rails testing [docs](http://guides.rubyonrails.org/testing.html). Life is struggle.

<!-- see https://github.com/Shopify/liquid/wiki/Liquid-for-Designers for stuff 
# H1
## H2
[I'm an inline-style link](https://www.google.com)
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png 'Logo Title Text 1')
```javascript
var s = 'JavaScript syntax highlighting';
alert(s);
```
   * an unordered list item (note a newline is required before the list begins)
   1. an ordered list item
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
-->
