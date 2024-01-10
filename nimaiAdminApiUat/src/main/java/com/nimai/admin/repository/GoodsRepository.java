package com.nimai.admin.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nimai.admin.model.Goods;



public interface GoodsRepository extends JpaRepository<Goods, Integer>
{

}
