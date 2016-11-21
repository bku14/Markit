//
//  ProfileViewController.swift
//  Markit
//
//  Created by Bryan Ku on 10/13/16.
//  Copyright © 2016 Victor Frolov. All rights reserved.
//

import UIKit

class ProfileViewController: UIViewController {
    
    @IBOutlet weak var pageControl: UIPageControl!
    @IBOutlet weak var containerView: UIView!
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let profilePageViewController = segue.destination as? ProfilePageViewController {
            profilePageViewController.profileDelegate = self
        }
    }
    
}


extension ProfileViewController: ProfilePageViewControllerDelegate {
    
    internal func profilePageViewController(profilePageViewController: ProfilePageViewController,
                                    didUpdatePageCount count: Int) {
        pageControl.numberOfPages = count
    }
    
    internal func profilePageViewController(profilePageViewController: ProfilePageViewController,
                                    didUpdatePageIndex index: Int) {
        pageControl.currentPage = index
    }
    
}
