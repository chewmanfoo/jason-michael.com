---
layout: post
title: "Conflicting scopes and how to make them play nice"
date: 2012-04-04 23:59
comments: true
categories: 
---
If you do enough with Rails 2, you'll be using named scopes (I think they're frowned upon in Rails 3).  I use named scopes for everything, particularly when I need to write complex queries which involve joins of tables.  In this example, I have these models: an Rca, a Root Cause Analysis document, which is a report on a system failure and why it happened, a GenericProfile, which is a generalization of a System which might have a failure, an ImplementationType, which is a way a System might be implemented, a SoftwareRelease, which is the software the GenericProfile (System) might be running, and a FeatureGroup, which is a collection of SoftwareReleases.  When talking about Rcas, we need to group them by ImplementationType and by FeatureGroup, so we can say "show me all the Rcas that refer to this System type", or "show me all the Rcas that refer to this method of implementation".  We can do this with named scopes.
<!-- more -->

Here's how I implemented the named scopes `by_feature_group` and `by_implementation_type`:

{% codeblock All of this is in models.rca.rb - by_feature_group.rb %}
  named_scope :by_feature_group,
    lambda {
    |p| {
      :select => ["rcas.name, rcas.id, rcas.carrier_id, rcas.generic_profile_id, rcas.event_start_a
t, rcas.event_end_at, rcas.include_in_sla_calc, rcas.affected_profiles, rcas.affects_tenants_but_no
t_host"],
      :conditions => ["feature_groups.id = ?", p ],
      :joins => [ "left join generic_profiles on rcas.generic_profile_id = generic_profiles.id left
 join software_releases on generic_profiles.current_sw_release_id = software_releases.id left join
feature_groups on software_releases.feature_group_id = feature_groups.id"]
        }
    }
{% endcodeblock %}

{% codeblock - by_implementation_type.rb %}
  named_scope :by_implementation_type,
    lambda {
    |t| {
      :select => ["rcas.name, rcas.id, rcas.carrier_id, rcas.generic_profile_id, rcas.event_start_a
t, rcas.event_end_at, rcas.include_in_sla_calc, rcas.affected_profiles, rcas.affects_tenants_but_no
t_host"],
      :joins => ["left join generic_profiles on rcas.generic_profile_id = generic_profiles.id"],
      :conditions => ["generic_profiles.implementation_type_id in (?)", t.join(',')]
        }
    }
{% endcodeblock %}

This all works fine if they're used separately:

{% codeblock - issues.rb %}
?> Rca.by_feature_group(1).size
=> 105
>> Rca.by_implementation_type([1]).size
=> 171
{% endcodeblock %}

But, if they're chained (as normal AR scopes can be easily), problems can occur:

{% codeblock - problems.rb %}
>> Rca.by_implementation_type([1]).by_feature_group(1).size
ActiveRecord::StatementInvalid: Mysql::Error: Not unique table/alias: 'generic_profiles': SELECT count(*) AS count_all FROM `rcas` left join generic_profiles on rcas.generic_profile_id = generic_profiles.id left join software_releases on generic_profiles.current_sw_release_id = software_releases.id left join feature_groups on software_releases.feature_group_id = feature_groups.id left join generic_profiles on rcas.generic_profile_id = generic_profiles.id WHERE ((feature_groups.id = 1) AND (generic_profiles.implementation_type_id in ('1')))
{% endcodeblock %}

Why is this happening?

It's happening because the magic of AR is merging the two complex queries together (for efficiency, and because it wouldn't work if it didn't), and the two queries aren't merging well.  Specifically, `by_feature_group` joins on generic_profiles, but so does `by_implementation_type`, so the query mentions generic_profiles more than once, and mysql can't tell which generic_profiles you're talking about.  If only mysql were smarter!  To solve this problem, we could do one of two things (the second is better):

 1. remove the separate scopes and write a single new scope `by_feature_group_by_implementation_type`
 1. alias generic_profiles in one of the scopes (let your hamster pick which one)

The second option is better because it still allows the separate scopes to remain separate. The new `by_implementation_type` scope now looks like this:

{% codeblock - by_implementation_type.rb %}
  named_scope :by_implementation_type,
    lambda { |a|
      a = Array(a)
      {
        :select => ["rcas.name, rcas.id, rcas.carrier_id, rcas.generic_profile_id, rcas.event_start_at, rcas.event_end_at, rcas.include_in_sla_calc, rcas.affected_profiles, rcas.affects_tenants_but_not_host"],
        :joins => ["left join generic_profiles as g on rcas.generic_profile_id = g.id"],
        :conditions => ["g.implementation_type_id in (?)", a.join(',')]
        }
    }
{% endcodeblock %}

Cool, eh?
