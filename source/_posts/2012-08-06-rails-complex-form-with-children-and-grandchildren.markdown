---
layout: post
title: "Rails Complex Form with Children and Grandchildren"
date: 2012-08-06 15:48
comments: true
categories: rails
---
I love Rails forms and the simplicity of AR associations in forms.  Ryab Bates explains in this railscast how easy it is to include child classes in associations in a form, and have all creates and updates happen automatically: [link](http://railscasts.com/episodes/196-nested-model-form-revised).  What Ryan doesn't cover, is the possibility that the model's child may have children (grandchildren).  

<!-- more -->
After fighting with this for a week or so, I figured it out.  It's all in the controller, where the child and grandchild is created at first:

{% codeblock scheduled_tests_controller.rb %}
  def new
    @scheduled_test = ScheduledTest.new

    1.times do
      wireless_client_test = @scheduled_test.wireless_client_tests.build
      1.times { wireless_client_test.build_wireless_client }
    end
  ...
{% endcodeblock %}

And in the helper method, where code is given to the javascript to add the new fields when "Add *" is clicked:

{% codeblock application.rb %}
  def special_button_to_add_fields(name, f, association, child_association)
    new_object = f.object.class.reflect_on_association(association).klass.new
    child_object = f.object.class.reflect_on_association(association).klass.reflect_on_association(child_association).klass.new
    new_object.wireless_client = child_object

    fields = f.fields_for(association, new_object, :child_index => "new_#{association}") do |builder|
      render(association.to_s.singularize + "_fields", :f => builder)
    end
    button_to_function(name, h("add_fields(this, \"#{association}\", \"#{escape_javascript(fields)}\")"))
  end
{% endcodeblock %}

Note that I had to change Ryan's helper adding in the second association and a bit of code to realize the grandchildren objects. Not only do you have to create the grandchild, but you have to associate the grandchild with the child so the form works properly.

There's probably a better solution.
