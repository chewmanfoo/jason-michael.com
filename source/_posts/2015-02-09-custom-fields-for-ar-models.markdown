---
layout: post
title: "Custom Fields for AR Models"
date: 2015-02-09 18:39
comments: true
categories: rails, AR
---
One of the difficulties of working with ORM frameworks like rails is that models which have database backends have schemas which are fixed at run time.  For
example, a `Post` model, it might have a `:title` field, and a `:body` field, and an `:author` reference.  But you can't, at runtime, add a `:photo`
field and a reference to S3 where the full-sized photo is stored and have the new data stored in the `Post` model and persist. (Well, you could, but you would
have to monkey-patch the application, and your app might go off the rails (no pun intended) if you're not careful. But a well-planned method for keeping the
custom fields marshalled, editable, validatable.
<!-- more -->
This page was a revelation to me: [link](http://railscasts.com/episodes/403-dynamic-forms?view=asciicast) (all railscasts usually are.)

So I want to make notes of Ryan's presentation so I can remember it later (and learn it better).

## The Setup
You have an online store which sells generic items - books, shirts, tools, computers etc. You want to keep a `Product` table, and not add sub-classes: a book
is a `Product`, a shirt is a `Product`, a tool is a `Product`. You could also have tons of fields in `Product` that are not used: a shirt with `:author_id` 
field which is nil, but that's very sloppy and it still can't deal with future products elegantly. A better way is to decorate the `Product` object with
fields that can be defined in the future. Once caveat - you can't support _every_ type, you have to prepare templates for the new field types (a template
for a check box, or a shirt size, or a wheel diameter). So your store could support a `Product` which is a bike with a specific wheel diameter, but it can't
suddenly support brake types (disc, friction, none) without a new build. Planning is still critical.

## Product supports ProductType
Step one is to allow `Product`s to have different types. A book `Product` is a `Product` of type 'book'. `Product`s can have only one type. `ProductType` 
objects only have a `:name` attribute, but serve as containers for associated `ProductField` objects which have a `:name`, `:field_type`, `:required` boolean
and an association to a ProductType. So `ProductType` needs to `accepts_nested_attributes_for` ProductField with javascripts for adding a new field and
removing a field inline with the form.

```ruby
class ProductType < ActiveRecord::Base
  has_many :products
  has_many :fields, class_name: "ProductField"
  accepts_nested_attributes_for :fields, allow_destroy: true, reject_if: proc { |attributes| attributes['name'].blank? }
```

```javascript
$(document).on 'click', 'form .add_fields', (event) ->
  time = new Date().getTime()
  regexp = new RegExp($(this).data('id'), 'g')
  $(this).before($(this).data('fields').replace(regexp, time))
  event.preventDefault()

$(document).on 'click', 'form .remove_fields', (event) ->
  $('input[name$="[_destroy]"]', $(this).siblings()).val('1')
  $(this).closest('fieldset').hide()
  event.preventDefault()
```

## Product form accepts data from the new ProductType.product_fields fields
So now to create a `Product` you have to first assign it's `ProductType`, then present a form for a `Product` plus all it's `ProductType.product_fields`. To
store the data in the `Product`, we'll stick it all in a `:properties` field with a `:text` type, but in a format which allows us to get at the data in an
orderly way any time we want. Rails provides a method for doing this with a `serialize` method, storing the values in a Hash. In order to get at that data
in a form, we use `fields_for :properties`. In order to de-serialize the `:properties` field, we have to mash it in and out of a Hash structure in the form,
and conveniently, Ruby provides a method for this, in `OpenStruct.new(@product.properties)`, which represents the `:properties` field, de-serialized.

```ruby
<%= form_for(@resource) do |f| %>
  <% if @resource.errors.any? %>
    <div id="error_explanation">
      <h2><%= pluralize(@resource.errors.count, "error") %> prohibited this resource from being saved:</h2>

      <ul>
      <% @resource.errors.full_messages.each do |message| %>
        <li><%= message %></li>
      <% end %>
      </ul>
    </div>
  <% end %>

<%= f.hidden_field :resource_type_id %>

  <%= render "common/forms/rounded_buttoned_labelled_text_field", :f => f,
                                                                  :name => :name,
                                                                  :real_name => "",
                                                                  :placeholder => "give it a name" %>

<%= f.fields_for :properties, OpenStruct.new(@resource.properties) do |builder| %>
  <% @resource.resource_type.fields.each do |field| %>
    <%= render "resource_types/fields/#{field.field_type}", field: field, f: builder %>
  <% end %>
<% end %>
```

## Product validation
If we add a `ProductField` to `Product`, we need to allow for the option of validation, in a limited way. In this case, we have validation for `presence`. In
order to accomplish this, we'll just create a custom validator which loops through all the `ProductField` values, looking for fields which are required,
and then look in the `params` hash to make sure it's there - if it isn't then add to the `errors` hash: `field.name, "must not be blank"`. 

```ruby
  validate :validate_properties

  def validate_properties
    resource_type.fields.each do |f|
      if f.required? && properties[f.name].blank?
        errors.add f.name, "must not be blank"
      end
    end
  end
```
   
## Dealing with the views
Now, when the user uses the form, they need a view of these `ProductField`s which is appropriate. A `check_box` needs a check box and a label. We can add a
partial for every field type we'll accept (this can be a global partial, not just specific to `Product`). *And finally* (whew!) in the show view, you have to
deal with the fields intelligently as well. This could also be with a partial, or simply in a `name:value` loop.

```ruby
app/views/product_types/fields/_check_box.html.erb

<div class="field">
  <%= render "common/forms/rounded_buttoned_labelled_check_box", :f => f,
                                                                 :name => field.name,
                                                                 :real_name => field.name %>
</div>
```

```ruby
app/views/common/forms/_rounded_buttoned_labelled_check_box.html.erb

<%= content_tag :div, class: "row collapse" do %>
<%= content_tag :div, class: "columns" do %>
  <%= content_tag :div, class: "row collapse prefix-round" do %>
    <%= content_tag :div, class: "small-3 columns" do %>
      <% if real_name.blank? %>
        <%= link_to name, "#", class: "button prefix" %>
      <% else %>
        <%= link_to real_name, "#", class: "button prefix" %>
      <% end %>
    <% end %>
    <%= content_tag :div, class: "small-9 columns" do %>
      <%= f.check_box name %> True
    <% end %>
  <% end %>
<% end %>
<% end %>
```

## New stuff
A few problems jump out at you when you implement this kind of solution. 1.) if you create a `ProductType` which contains 3 fields, and then you create a
`Product` of that `ProductType` and then you release it into the wild, pretty soon you're gonna have users who want to _change stuff_ they've already created.
For example, Sally comes to you, Mr Developer, and says "change all `shirt_size` fields to `size`". Sally can go suck an egg, for sure, but the problem still
remains in that this kind of change requires some unique code, searching within properties for specific keys and replacing them as needed. 2.) worse, this 
approach doesn't work very well for more complex data types, like "either-or", which requires labels, or multiple select. Ultimately, you're having to deal
with implementing round-trip editing of a complex data type where the `properties` hash might contain arrays, or other hashes.  Makes my head hurt.
  
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
