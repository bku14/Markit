//
//  ChatViewController.swift
//  Markit
//
//  Created by Trixie on 11/29/16.
//  Copyright © 2016 Victor Frolov. All rights reserved.
//

import UIKit
import Firebase

class ChatListViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {

    @IBOutlet weak var chatTableView: UITableView!
    
    var conversations = [FIRDataSnapshot]()
    var databaseRef: FIRDatabaseReference!
    var userRef:     FIRDatabaseReference!
    var chatRef:     FIRDatabaseReference!
    var currentUser: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        currentUser = CustomFirebaseAuth().getCurrentUser()
        
        databaseRef = FIRDatabase.database().reference()
        userRef     = databaseRef.child("users").child(currentUser)
        chatRef     = userRef.child("chats")
        
        chatTableView.delegate = self
        chatTableView.dataSource = self
        
        getMessages()
        
        chatTableView.reloadData()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func getMessages() {
        chatRef.queryOrdered(byChild: "latestPost").observe(.childAdded, with: { (snapshot) -> Void in
            self.conversations.append(snapshot)
            
            self.chatTableView.reloadData()
        })
    }
    
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return conversations.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellID = "ChatCell"
        let row = indexPath.row
        let cell   = tableView.dequeueReusableCell(withIdentifier: cellID, for: indexPath) as! ChatTableViewCell
        
        let conversation = conversations[row].value as! NSDictionary
        let context      = conversation["context"] as! NSDictionary
        let message      = conversation["messages"] as! NSDictionary
        
        cell.chatUsername?.text       = context["otherUser"] as! String?
//        cell.chatImageView?.image = //get image from url -> context["itemImageURL"] as! String?)
        cell.lastSent?.text           = context["latestPost"] as! String?
        cell.chatMessagePreview?.text = message["message"] as! String?
        
        return cell
    }

    // MARK: - Table view delegate
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        chatTableView.deselectRow(at: indexPath, animated: true)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "viewRecentMessageSegue" {
            
            if let indexPath = chatTableView.indexPathForSelectedRow {
                let selectedRow         = indexPath.row
                let selectedConvo       = conversations[selectedRow].value as! NSDictionary
                let context             = selectedConvo["context"] as! NSDictionary
                let messageVC           = segue.destination as! ChatMessageViewController
                
                messageVC.context       = context["conversationID"] as! String?
                messageVC.senderId      = currentUser
                messageVC.itemID        = context["itemID"] as! String?
                messageVC.otherUserID   = context["otherUser"] as! String?
                messageVC.otherUserName = context["otherUseName"] as! String?
            }
        }
    }
}
