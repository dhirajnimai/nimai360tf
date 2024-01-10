package com.nimai.splan.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "nimai_m_subscriptioncountry")
@EntityListeners({AuditingEntityListener.class})
@DynamicUpdate
public class NimaiMSubscriptionCountry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "sub_country_id")
  private Integer id;
  
  @Column(name = "subscription_plan_id")
  private int subscriptionPlanId;
  
  @Column(name = "subscription_id")
  private String subscriptionId;
  
  @Column(name = "subscription_country")
  private String country;
  
  public int getSubscriptionPlanId() {
    return this.subscriptionPlanId;
  }
  
  public void setSubscriptionPlanId(int subscriptionPlanId) {
    this.subscriptionPlanId = subscriptionPlanId;
  }
  
  public Integer getId() {
    return this.id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
  
  public String getSubscriptionId() {
    return this.subscriptionId;
  }
  
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }
  
  public String getCountry() {
    return this.country;
  }
  
  public void setCountry(String country) {
    this.country = country;
  }
  
  public String toString() {
    return "NimaiMSubscriptionCountry [id=" + this.id + ", userId=, subscriptionId=" + this.subscriptionId + ", country=" + this.country + "]";
  }
}
